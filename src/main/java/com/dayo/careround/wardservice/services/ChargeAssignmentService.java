package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.ChargeAssignment;
import com.dayo.careround.wardservice.data.models.ShiftDefinition;
import com.dayo.careround.wardservice.data.models.enums.ChargeAssignmentStatus;
import com.dayo.careround.wardservice.data.repositories.ChargeAssignmentRepository;
import com.dayo.careround.wardservice.data.repositories.ShiftDefinitionRepository;
import com.dayo.careround.wardservice.dto.request.GrantChargeAssignmentRequest;
import com.dayo.careround.wardservice.dto.response.ChargeAssignmentResponse;
import com.dayo.careround.wardservice.exception.ShiftDefinitionNotFoundException;
import jakarta.transaction.Transactional;
import com.dayo.careround.wardservice.data.models.enums.AttendanceStatus;
import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import com.dayo.careround.wardservice.data.repositories.AttendanceRepository;
import com.dayo.careround.wardservice.data.repositories.WardRepository;
import com.dayo.careround.wardservice.exception.NoEligibleNurseException;
import com.dayo.careround.wardservice.exception.WardNotFoundException;
import com.dayo.careround.wardservice.security.CareRoundPrincipal;
import org.springframework.security.access.AccessDeniedException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChargeAssignmentService {

    private final ChargeAssignmentRepository chargeAssignmentRepository;
    private final ShiftDefinitionRepository shiftDefinitionRepository;
    private final WardRepository wardRepository;
    private final AttendanceRepository attendanceRepository;
    private final AuditLogService auditLogService;

    /**
     * Grant WARD_SUPERVISION for a ward. Allowed for an ADMIN with GRANT_PERMISSIONS, or for the nurse currently
     * holding charge on that ward handing it to another nurse who is clocked in on the ward (ward-service only knows
     * a nurse's ward through attendance). A nurse cannot grant charge to themselves. Audited (chain of custody).
     */
    @Transactional
    public ChargeAssignmentResponse grant(CareRoundPrincipal caller, boolean adminWithGrantPermission,
                                          GrantChargeAssignmentRequest request) {
        if (wardRepository.findByPublicId(request.wardId()).isEmpty()) {
            throw new WardNotFoundException("No ward with that id");
        }
        Optional<ChargeAssignment> current = chargeAssignmentRepository.findByWardIdAndStatus(request.wardId(), ChargeAssignmentStatus.ACTIVE);
        String actorRole;
        if ("ADMIN".equals(caller.role()) && adminWithGrantPermission) {
            actorRole = "ADMIN";
        } else if ("NURSE".equals(caller.role()) && current.map(ca -> ca.getNurseId().equals(caller.userId())).orElse(false)) {
            if (request.nurseId().equals(caller.userId())) {
                throw new AccessDeniedException("You already hold charge for this ward");
            }
            boolean targetOnWard = attendanceRepository.findByStaffIdAndStatus(request.nurseId(), AttendanceStatus.CLOCKED_IN)
                    .filter(a -> a.getWardId().equals(request.wardId()) && a.getStaffRole() == StaffingRole.NURSE)
                    .isPresent();
            if (!targetOnWard) {
                throw new NoEligibleNurseException("Charge can only be transferred to a nurse clocked in on this ward");
            }
            actorRole = "CHARGE_NURSE";
        } else {
            throw new AccessDeniedException("Only an admin or the ward's current charge nurse may grant charge");
        }
        UUID grantedBy = caller.userId();
        // Expire any currently ACTIVE assignment for this ward first —
        // exactly one ACTIVE per ward, per the PRD's state machine
        chargeAssignmentRepository.findByWardIdAndStatus(request.wardId(), ChargeAssignmentStatus.ACTIVE)
                .ifPresent(existing -> {
                    existing.setStatus(ChargeAssignmentStatus.TRANSFERRED);
                    existing.setExpiresAt(Instant.now());
                    chargeAssignmentRepository.save(existing);
                });

        ShiftDefinition shift = shiftDefinitionRepository.findById(request.shiftId())
                .orElseThrow(() -> new ShiftDefinitionNotFoundException("No shift with that id"));

        ChargeAssignment assignment = new ChargeAssignment();
        assignment.setWardId(request.wardId());
        assignment.setNurseId(request.nurseId());
        assignment.setShift(shift);
        assignment.setStatus(ChargeAssignmentStatus.ACTIVE);
        assignment.setGrantedBy(grantedBy);
        assignment.setGrantedAt(Instant.now());

        chargeAssignmentRepository.save(assignment);
        auditLogService.log(caller.userId(), actorRole, current.isPresent() ? "CHARGE_TRANSFERRED" : "CHARGE_GRANTED",
                "ChargeAssignment", assignment.getId(), request.wardId(),
                "Charge granted to " + request.nurseId() + current.map(ca -> " (from " + ca.getNurseId() + ")").orElse(""));
        return toResponse(assignment);
    }

    @Transactional
    public ChargeAssignmentResponse getActiveForWard(UUID wardId) {
        ChargeAssignment assignment = chargeAssignmentRepository
                .findByWardIdAndStatus(wardId, ChargeAssignmentStatus.ACTIVE)
                .orElseThrow(() -> new ShiftDefinitionNotFoundException("No active charge assignment for this ward"));
        return toResponse(assignment);
    }

    private ChargeAssignmentResponse toResponse(ChargeAssignment assignment) {
        return new ChargeAssignmentResponse(
                assignment.getId(), assignment.getWardId(), assignment.getNurseId(),
                assignment.getShift().getName(), assignment.getStatus(),
                assignment.getGrantedBy(), assignment.getGrantedAt(), assignment.getExpiresAt()
        );
    }
}