package com.dayo.careround.wardservice.dto.request;

import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import com.dayo.careround.wardservice.data.models.enums.StaffingScope;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateStaffingRequirementRequest(
        @NotNull StaffingScope scope,
        UUID wardId, // required only when scope = WARD_OVERRIDE, validated in service
        @NotNull StaffingRole role,
        @NotNull UUID shiftId,
        @Positive int ratio,
        @Positive int floor
) {}