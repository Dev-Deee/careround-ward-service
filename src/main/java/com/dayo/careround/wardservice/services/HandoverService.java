package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.Handover;
import com.dayo.careround.wardservice.data.models.HandoverNote;
import com.dayo.careround.wardservice.data.models.ShiftDefinition;
import com.dayo.careround.wardservice.data.models.enums.HandoverStatus;
import com.dayo.careround.wardservice.data.models.enums.HandoverUrgency;
import com.dayo.careround.wardservice.data.repositories.HandoverNoteRepository;
import com.dayo.careround.wardservice.data.repositories.HandoverRepository;
import com.dayo.careround.wardservice.dto.request.InitiateHandoverRequest;
import com.dayo.careround.wardservice.dto.response.HandoverResponse;
import com.dayo.careround.wardservice.exception.HandoverNotFoundException;
import jakarta.transaction.Transactional;
import com.dayo.careround.wardservice.data.models.enums.ChargeAssignmentStatus;
import com.dayo.careround.wardservice.data.repositories.ChargeAssignmentRepository;
import com.dayo.careround.wardservice.exception.InvalidHandoverException;
import org.springframework.security.access.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HandoverService {

    private final HandoverRepository handoverRepository;
    private final HandoverNoteRepository handoverNoteRepository;
    private final ShiftResolutionService shiftResolutionService;
    private final ChargeAssignmentRepository chargeAssignmentRepository;

    public boolean isSupervisionHandedOver(UUID wardId, UUID outgoingChargeNurseId) {
        return handoverRepository
                .findByWardIdAndOutgoingChargeNurseIdAndStatus(wardId, outgoingChargeNurseId, HandoverStatus.HANDED_OVER)
                .isPresent();
    }

    @Transactional
    // Only the nurse currently holding charge on the ward hands it over (they become the outgoing charge nurse).
    public HandoverResponse initiate(UUID wardId, UUID outgoingChargeNurseId, InitiateHandoverRequest request) {
        if (!isActiveChargeHolder(wardId, outgoingChargeNurseId)) {
            throw new AccessDeniedException("Only the ward's current charge nurse may start a handover");
        }
        if (request.incomingChargeNurseId().equals(outgoingChargeNurseId)) {
            throw new InvalidHandoverException("The incoming charge nurse must be someone else");
        }
        ShiftDefinition currentShift = shiftResolutionService.resolveCurrentShift();

        Handover handover = new Handover();
        handover.setWardId(wardId);
        handover.setOutgoingShift(currentShift);
        handover.setIncomingShift(currentShift); // simplification: same definition, next occurrence
        handover.setOutgoingChargeNurseId(outgoingChargeNurseId);
        handover.setIncomingChargeNurseId(request.incomingChargeNurseId());
        handover.setStatus(HandoverStatus.PENDING);

        handoverRepository.save(handover);
        return toResponse(handover);
    }

    @Transactional
    public void addNote(UUID handoverPublicId, UUID callerId, UUID patientId, String note, HandoverUrgency urgency) {
        Handover handover = handoverRepository.findByPublicId(handoverPublicId)
                .orElseThrow(() -> new HandoverNotFoundException("No handover with that id"));
        requireParticipant(handover, callerId);

        HandoverNote handoverNote = new HandoverNote();
        handoverNote.setHandoverId(handover.getId());
        handoverNote.setPatientId(patientId);
        handoverNote.setNote(note);
        handoverNote.setUrgency(urgency);
        handoverNoteRepository.save(handoverNote);
    }

    public void signOff(UUID handoverPublicId, UUID callerId) {
        Handover handover = handoverRepository.findByPublicId(handoverPublicId)
                .orElseThrow(() -> new HandoverNotFoundException("No handover with that id"));
        requireParticipant(handover, callerId);
        if (handover.getStatus() == HandoverStatus.HANDED_OVER) {
            throw new InvalidHandoverException("This handover is already signed off");
        }
        handover.setStatus(HandoverStatus.HANDED_OVER);
        handover.setSignedOffAt(Instant.now());
        handoverRepository.save(handover);
    }
    // The outgoing or incoming nurse named on the handover, or whoever holds charge on that ward now.
    private void requireParticipant(Handover handover, UUID callerId) {
        boolean named = callerId.equals(handover.getOutgoingChargeNurseId()) || callerId.equals(handover.getIncomingChargeNurseId());
        if (!named && !isActiveChargeHolder(handover.getWardId(), callerId)) {
            throw new AccessDeniedException("Only the nurses named on this handover or the ward's charge nurse may change it");
        }
    }

    private boolean isActiveChargeHolder(UUID wardId, UUID nurseId) {
        return chargeAssignmentRepository.findByWardIdAndStatus(wardId, ChargeAssignmentStatus.ACTIVE)
                .map(ca -> ca.getNurseId().equals(nurseId))
                .orElse(false);
    }

    private HandoverResponse toResponse(Handover handover) {
        return new HandoverResponse(
                handover.getPublicId(),
                handover.getWardId(),
                handover.getOutgoingChargeNurseId(),
                handover.getIncomingChargeNurseId(),
                handover.getStatus(),
                handover.getInitiatedAt(),
                handover.getSignedOffAt()
        );
    }
}