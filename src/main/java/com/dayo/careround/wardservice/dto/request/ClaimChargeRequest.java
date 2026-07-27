package com.dayo.careround.wardservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ClaimChargeRequest(
        @NotNull UUID wardId
) {}