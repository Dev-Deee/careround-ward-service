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
    public AttendanceResponse clockIn(UUID staffId, ClockInRequest request) {
        if (attendanceRepository.findByStaffIdAndStatus(staffId, AttendanceStatus.CLOCKED_IN).isPresent()) {
            throw new AlreadyClockedInException("This staff member is already clocked in");
        }

        ShiftDefinition currentShift = shiftResolutionService.resolveCurrentShift();

        Attendance attendance = new Attendance();
        attendance.setStaffId(staffId);
        attendance.setStaffRole(request.staffRole());
        attendance.setWardId(request.wardId());
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
                .orElseThrow(() -> new IllegalStateException("No active clock-in found for this staff member"));

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
}