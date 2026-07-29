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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChargeAssignmentService {

    private final ChargeAssignmentRepository chargeAssignmentRepository;
    private final ShiftDefinitionRepository shiftDefinitionRepository;

    @Transactional
    public ChargeAssignmentResponse grant(UUID grantedBy, GrantChargeAssignmentRequest request) {
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