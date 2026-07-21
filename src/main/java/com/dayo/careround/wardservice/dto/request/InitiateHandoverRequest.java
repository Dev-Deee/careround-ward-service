package com.dayo.careround.wardservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InitiateHandoverRequest(
        @NotNull UUID wardId,
        @NotNull UUID incomingChargeNurseId
) {}