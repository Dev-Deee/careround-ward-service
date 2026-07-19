package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.Handover;
import com.dayo.careround.wardservice.data.models.HandoverNote;
import com.dayo.careround.wardservice.data.models.enums.HandoverStatus;
import com.dayo.careround.wardservice.data.repositories.HandoverNoteRepository;
import com.dayo.careround.wardservice.data.repositories.HandoverRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HandoverService {

    private final HandoverRepository handoverRepository;
    private final HandoverNoteRepository handoverNoteRepository;

    public boolean isSupervisionHandedOver(UUID wardId, UUID outgoingChargeNurseId) {
        return handoverRepository
                .findByWardIdAndOutgoingChargeNurseIdAndStatus(wardId, outgoingChargeNurseId, HandoverStatus.HANDED_OVER)
                .isPresent();
    }

    @Transactional
    public Handover initiate(UUID wardId, UUID outgoingShiftId, UUID incomingShiftId,
                             UUID outgoingChargeNurseId, UUID incomingChargeNurseId) {
        Handover handover = new Handover();
        handover.setWardId(wardId);
        // outgoingShift/incomingShift set via repository lookups in the real service —
        // simplified here, wire in ShiftDefinitionRepository when building the controller layer
        handover.setOutgoingChargeNurseId(outgoingChargeNurseId);
        handover.setIncomingChargeNurseId(incomingChargeNurseId);
        handover.setStatus(HandoverStatus.PENDING);
        handoverRepository.save(handover);
        return handover;
    }

    @Transactional
    public void addNote(UUID handoverId, UUID patientId, String note, com.dayo.careround.wardservice.data.models.enums.HandoverUrgency urgency) {
        HandoverNote handoverNote = new HandoverNote();
        handoverNote.setHandoverId(handoverId);
        handoverNote.setPatientId(patientId);
        handoverNote.setNote(note);
        handoverNote.setUrgency(urgency);
        handoverNoteRepository.save(handoverNote);
    }

    @Transactional
    public void signOff(UUID handoverId) {
        Handover handover = handoverRepository.findById(handoverId)
                .orElseThrow(() -> new IllegalStateException("Handover not found"));
        handover.setStatus(HandoverStatus.HANDED_OVER);
        handover.setSignedOffAt(Instant.now());
        handoverRepository.save(handover);
    }
}