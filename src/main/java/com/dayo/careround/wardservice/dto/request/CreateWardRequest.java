package com.dayo.careround.wardservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateWardRequest(
        @NotBlank String name,
        @NotNull Integer capacity
) {}
