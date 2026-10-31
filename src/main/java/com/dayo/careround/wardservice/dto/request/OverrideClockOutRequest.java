package com.dayo.careround.wardservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// staffId: the user id (JWT sub) of the staff member being clocked out despite the gate.
public record OverrideClockOutRequest(
        @NotNull UUID staffId,
        @NotBlank String reason
) {}
