package com.dayo.careround.wardservice.dto.response;

import com.dayo.careround.wardservice.data.models.enums.HandoverStatus;

import java.time.Instant;
import java.util.UUID;

public record HandoverResponse(
        UUID publicId,
        UUID wardId,
        UUID outgoingChargeNurseId,
        UUID incomingChargeNurseId,
        HandoverStatus status,
        Instant initiatedAt,
        Instant signedOffAt
) {}