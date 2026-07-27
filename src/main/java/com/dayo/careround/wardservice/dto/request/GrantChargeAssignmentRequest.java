package com.dayo.careround.wardservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GrantChargeAssignmentRequest(
        @NotNull UUID wardId,
        @NotNull UUID nurseId,
        @NotNull UUID shiftId
) {}