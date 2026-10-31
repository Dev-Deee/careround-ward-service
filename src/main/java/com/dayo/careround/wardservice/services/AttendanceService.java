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

    @Transactional
    public void clockOut(UUID staffId, boolean overrideAuthorized, UUID overriddenBy) {
        Attendance attendance = attendanceRepository
                .findByStaffIdAndStatus(staffId, AttendanceStatus.CLOCKED_IN)
                .orElseThrow(() -> new NoActiveAttendanceException("You have no active clock-in"));

        boolean isChargeNurse = chargeAssignmentRepository
                .findByWardIdAndStatus(attendance.getWardId(), ChargeAssignmentStatus.ACTIVE)
                .map(ca -> ca.getNurseId().equals(staffId))
                .orElse(false);

        // FR-10.5: charge nurse is double-gated — coverage AND supervision handover
        if (isChargeNurse) {
            boolean handedOver = handoverService.isSupervisionHandedOver(attendance.getWardId(), staffId);
            if (!handedOver && !overrideAuthorized) {
                throw new ClockOutBlockedException(
                        "WARD_SUPERVISION must be handed over before clocking out");
            }
        }

        boolean breach = coverageGateService.wouldBreachCoverage(
                attendance.getWardId(), attendance.getStaffRole(),
                attendance.getShift().getId(), staffId);

        if (breach && !overrideAuthorized) {
            throw new ClockOutBlockedException(
                    "Clocking out would drop ward below required staffing coverage");
        }

        attendance.setClockOutAt(Instant.now());
        attendance.setStatus(AttendanceStatus.CLOCKED_OUT);
        attendanceRepository.save(attendance);

        if (overrideAuthorized) {
            auditLogService.log(
                    overriddenBy, "ADMIN", "COVERAGE_GATE_OVERRIDE",
                    "Attendance", attendance.getId(), attendance.getWardId(),
                    "Clock-out authorized despite coverage/handover gate"
            );
        }
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
