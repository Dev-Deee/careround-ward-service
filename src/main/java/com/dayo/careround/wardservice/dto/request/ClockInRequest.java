package com.dayo.careround.wardservice.dto.request;

import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ClockInRequest(
        @NotNull UUID wardId,
        @NotNull StaffingRole staffRole
) {}