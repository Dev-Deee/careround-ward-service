package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.Attendance;
import com.dayo.careround.wardservice.data.models.ShiftDefinition;
import com.dayo.careround.wardservice.data.models.enums.AttendanceStatus;
import com.dayo.careround.wardservice.data.models.enums.ChargeAssignmentStatus;
import com.dayo.careround.wardservice.data.repositories.AttendanceRepository;
import com.dayo.careround.wardservice.data.repositories.ChargeAssignmentRepository;
import com.dayo.careround.wardservice.dto.request.ClockInRequest;
import com.dayo.careround.wardservice.dto.response.AttendanceResponse;
import com.dayo.careround.wardservice.exception.AlreadyClockedInException;
import com.dayo.careround.wardservice.exception.ClockOutBlockedException;
import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import com.dayo.careround.wardservice.exception.NoActiveAttendanceException;
import com.dayo.careround.wardservice.security.CareRoundPrincipal;
import org.springframework.security.access.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final CoverageGateService coverageGateService;
    private final ChargeAssignmentRepository chargeAssignmentRepository;
    private final HandoverService handoverService;
    private final AuditLogService auditLogService;
    private final ShiftResolutionService shiftResolutionService;

    @Transactional
    public AttendanceResponse clockIn(CareRoundPrincipal caller, ClockInRequest request) {
        StaffingRole role = clinicalRole(caller);
        UUID wardId = caller.assignedWardId();
        if (wardId == null) {
            throw new AccessDeniedException("You have no assigned ward to clock in to");
        }
        if (request != null && request.wardId() != null && !request.wardId().equals(wardId)) {
            throw new AccessDeniedException("You may only clock in to your assigned ward");
        }
        if (request != null && request.staffRole() != null && request.staffRole() != role) {
            throw new AccessDeniedException("staffRole must match your role (" + role + ")");
        }
        UUID staffId = caller.userId();
        if (attendanceRepository.findByStaffIdAndStatus(staffId, AttendanceStatus.CLOCKED_IN).isPresent()) {
            throw new AlreadyClockedInException("This staff member is already clocked in");
        }

        ShiftDefinition currentShift = shiftResolutionService.resolveCurrentShift();

        Attendance attendance = new Attendance();
        attendance.setStaffId(staffId);
        attendance.setStaffRole(role);
        attendance.setWardId(wardId);
        attendance.setShift(currentShift);
        attendance.setClockInAt(Instant.now());
        attendance.setStatus(AttendanceStatus.CLOCKED_IN);

        attendanceRepository.save(attendance);
        return toResponse(attendance);
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getId(), attendance.getStaffId(), attendance.getWardId(),
                attendance.getShift().getName(), attendance.getClockInAt(),
                attendance.getClockOutAt(), attendance.getStatus()
        );
    }

    // The caller's own clock-out: blocked by the coverage gate and, for the charge nurse, by the handover gate.
    @Transactional
    public void clockOut(UUID staffId) {
        Attendance attendance = attendanceRepository
                .findByStaffIdAndStatus(staffId, AttendanceStatus.CLOCKED_IN)
                .orElseThrow(() -> new NoActiveAttendanceException("You have no active clock-in"));

        boolean isChargeNurse = chargeAssignmentRepository
                .findByWardIdAndStatus(attendance.getWardId(), ChargeAssignmentStatus.ACTIVE)
                .map(ca -> ca.getNurseId().equals(staffId))
                .orElse(false);

        // FR-10.5: charge nurse is double-gated — coverage AND supervision handover
        if (isChargeNurse && !handoverService.isSupervisionHandedOver(attendance.getWardId(), staffId)) {
            throw new ClockOutBlockedException("WARD_SUPERVISION must be handed over before clocking out");
        }

        if (coverageGateService.wouldBreachCoverage(
                attendance.getWardId(), attendance.getStaffRole(), attendance.getShift().getId(), staffId)) {
            throw new ClockOutBlockedException("Clocking out would drop ward below required staffing coverage");
        }

        close(attendance);
    }

    /**
     * FR-10.4/10.6: an authorised override clocks a staff member out despite the gates. Allowed for the ward's
     * active charge holder (for someone else, never themselves) or an ADMIN with MANAGE_SYSTEM_CONFIG. Always
     * audited with the real actor, role and reason.
     */
    @Transactional
    public void overrideClockOut(CareRoundPrincipal caller, boolean adminWithConfigPermission, UUID staffId, String reason) {
        Attendance attendance = attendanceRepository
                .findByStaffIdAndStatus(staffId, AttendanceStatus.CLOCKED_IN)
                .orElseThrow(() -> new NoActiveAttendanceException("That staff member has no active clock-in"));

        String actorRole;
        if ("ADMIN".equals(caller.role()) && adminWithConfigPermission) {
            actorRole = "ADMIN";
        } else if ("NURSE".equals(caller.role()) && isActiveChargeHolder(attendance.getWardId(), caller.userId())) {
            if (caller.userId().equals(staffId)) {
                throw new AccessDeniedException("The charge nurse cannot override their own clock-out; an admin must authorise it");
            }
            actorRole = "CHARGE_NURSE";
        } else {
            throw new AccessDeniedException("Only the ward's charge nurse or an admin may authorise a clock-out override");
        }

        close(attendance);
        auditLogService.log(caller.userId(), actorRole, "COVERAGE_GATE_OVERRIDE", "Attendance", attendance.getId(),
                attendance.getWardId(), "Clock-out of " + staffId + " authorised despite coverage/handover gate: " + reason);
    }

    private boolean isActiveChargeHolder(UUID wardId, UUID nurseId) {
        return chargeAssignmentRepository.findByWardIdAndStatus(wardId, ChargeAssignmentStatus.ACTIVE)
                .map(ca -> ca.getNurseId().equals(nurseId))
                .orElse(false);
    }

    private void close(Attendance attendance) {
        attendance.setClockOutAt(Instant.now());
        attendance.setStatus(AttendanceStatus.CLOCKED_OUT);
        attendanceRepository.save(attendance);
    }

    // Only clinical staff clock in; ADMIN is not a ward staffing actor.
    private static StaffingRole clinicalRole(CareRoundPrincipal caller) {
        return switch (caller.role()) {
            case "NURSE" -> StaffingRole.NURSE;
            case "DOCTOR" -> StaffingRole.DOCTOR;
            default -> throw new AccessDeniedException("Only nurses and doctors clock in");
        };
    }
}
