package com.dayo.careround.wardservice.dto.response;

import com.dayo.careround.wardservice.data.models.enums.HandoverStatus;
import com.dayo.careround.wardservice.data.models.enums.HandoverUrgency;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record HandoverDetailResponse(
        UUID publicId,
        UUID wardId,
        UUID outgoingChargeNurseId,
        UUID incomingChargeNurseId,
        String outgoingShiftName,
        String incomingShiftName,
        HandoverStatus status,
        Instant initiatedAt,
        Instant signedOffAt,
        List<Note> notes
) {
    public record Note(UUID id, UUID patientId, String note, HandoverUrgency urgency) {}
}
