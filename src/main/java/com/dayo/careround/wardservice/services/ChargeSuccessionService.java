package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.Attendance;
import com.dayo.careround.wardservice.data.models.ChargeAssignment;
import com.dayo.careround.wardservice.data.models.ChargeSuccessionWindow;
import com.dayo.careround.wardservice.data.models.ShiftDefinition;
import com.dayo.careround.wardservice.data.models.enums.AttendanceStatus;
import com.dayo.careround.wardservice.data.models.enums.ChargeAssignmentStatus;
import com.dayo.careround.wardservice.data.models.enums.SuccessionStatus;
import com.dayo.careround.wardservice.data.repositories.AttendanceRepository;
import com.dayo.careround.wardservice.data.repositories.ChargeAssignmentRepository;
import com.dayo.careround.wardservice.data.repositories.ChargeSuccessionWindowRepository;
import com.dayo.careround.wardservice.exception.NoEligibleNurseException;
import com.dayo.careround.wardservice.exception.SuccessionWindowNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargeSuccessionService {

    private static final Duration CLAIM_WINDOW = Duration.ofMinutes(15);

    private final ChargeSuccessionWindowRepository successionWindowRepository;
    private final ChargeAssignmentRepository chargeAssignmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AuditLogService auditLogService;
    private final NurseSeniorityClient nurseSeniorityClient;

    /**
     * Called when the system detects the active charge nurse never clocked in
     * for their shift (e.g. by a scheduled check at shift start).
     */
    @Transactional
    public void openSuccessionWindow(UUID wardId, ShiftDefinition shift, UUID vacatedNurseId) {
        boolean alreadyOpen = successionWindowRepository
                .findByWardIdAndStatus(wardId, SuccessionStatus.OPEN).isPresent();
        if (alreadyOpen) return;

        ChargeSuccessionWindow window = new ChargeSuccessionWindow();
        window.setWardId(wardId);
        window.setShift(shift);
        window.setVacatedNurseId(vacatedNurseId);
        window.setOpenedAt(Instant.now());
        window.setClaimDeadline(Instant.now().plus(CLAIM_WINDOW));
        window.setStatus(SuccessionStatus.OPEN);

        successionWindowRepository.save(window);
        log.warn("Charge succession window opened for ward {} — nurse {} did not clock in", wardId, vacatedNurseId);
    }

    /**
     * A nurse actively claims charge before the window times out.
     */
    @Transactional
    public void claim(UUID wardId, UUID claimingNurseId) {
        ChargeSuccessionWindow window = successionWindowRepository
                .findByWardIdAndStatus(wardId, SuccessionStatus.OPEN)
                .orElseThrow(() -> new SuccessionWindowNotFoundException("No open succession window for this ward"));

        boolean isClockedIn = attendanceRepository
                .findByStaffIdAndStatus(claimingNurseId, AttendanceStatus.CLOCKED_IN)
                .filter(a -> a.getWardId().equals(wardId)
                        && a.getStaffRole() == com.dayo.careround.wardservice.data.models.enums.StaffingRole.NURSE)
                .isPresent();
        if (!isClockedIn) {
            throw new NoEligibleNurseException("Only a nurse currently clocked in on this ward may claim charge");
        }

        window.setStatus(SuccessionStatus.CLAIMED);
        window.setClaimedByNurseId(claimingNurseId);
        successionWindowRepository.save(window);

        grantCharge(wardId, window.getShift(), claimingNurseId, claimingNurseId);

        auditLogService.log(claimingNurseId, "NURSE", "CHARGE_CLAIMED",
                "ChargeSuccessionWindow", window.getId(), wardId,
                "Nurse claimed vacant charge assignment");
    }

    /**
     * Scheduled sweep — auto-assigns any window whose claim deadline has passed unclaimed.
     */

    @Transactional
    public void resolveExpiredWindows() {
        List<ChargeSuccessionWindow> expired = successionWindowRepository
                .findByStatusAndClaimDeadlineBefore(SuccessionStatus.OPEN, Instant.now());

        for (ChargeSuccessionWindow window : expired) {
            List<Attendance> eligible = attendanceRepository
                    .findByWardIdAndStaffRoleAndStatus(window.getWardId(),
                            com.dayo.careround.wardservice.data.models.enums.StaffingRole.NURSE,
                            AttendanceStatus.CLOCKED_IN);

            if (eligible.isEmpty()) {
                log.error("Charge succession window for ward {} expired with NO eligible nurses clocked in — " +
                        "ward has no charge coverage, requires immediate admin intervention", window.getWardId());
                continue;
            }

            UUID mostSeniorNurseId = nurseSeniorityClient.getMostSeniorNurseId(eligible);

            window.setStatus(SuccessionStatus.AUTO_ASSIGNED);
            window.setClaimedByNurseId(mostSeniorNurseId);
            successionWindowRepository.save(window);

            grantCharge(window.getWardId(), window.getShift(), mostSeniorNurseId, null);

            auditLogService.log(mostSeniorNurseId, "SYSTEM", "CHARGE_AUTO_ASSIGNED",
                    "ChargeSuccessionWindow", window.getId(), window.getWardId(),
                    "Auto-assigned after claim window expired unclaimed — resolved via grade+clock-in tie-break");
        }
    }

    private void grantCharge(UUID wardId, ShiftDefinition shift, UUID nurseId, UUID grantedBy) {
        chargeAssignmentRepository.findByWardIdAndStatus(wardId, ChargeAssignmentStatus.ACTIVE)
                .ifPresent(existing -> {
                    existing.setStatus(ChargeAssignmentStatus.TRANSFERRED);
                    existing.setExpiresAt(Instant.now());
                    chargeAssignmentRepository.save(existing);
                });

        ChargeAssignment assignment = new ChargeAssignment();
        assignment.setWardId(wardId);
        assignment.setNurseId(nurseId);
        assignment.setShift(shift);
        assignment.setStatus(ChargeAssignmentStatus.ACTIVE);
        assignment.setGrantedBy(grantedBy != null ? grantedBy : nurseId);
        assignment.setGrantedAt(Instant.now());
        chargeAssignmentRepository.save(assignment);
    }
}