package com.dayo.careround.wardservice.dto.response;

import com.dayo.careround.wardservice.data.models.enums.ChargeAssignmentStatus;

import java.time.Instant;
import java.util.UUID;

public record ChargeAssignmentResponse(
        UUID id,
        UUID wardId,
        UUID nurseId,
        String shiftName,
        ChargeAssignmentStatus status,
        UUID grantedBy,
        Instant grantedAt,
        Instant expiresAt
) {}