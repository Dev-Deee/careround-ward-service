package com.dayo.careround.wardservice.dto.response;

import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import com.dayo.careround.wardservice.data.models.enums.StaffingScope;

import java.util.UUID;

public record StaffingRequirementResponse(
        UUID id,
        StaffingScope scope,
        UUID wardId,
        StaffingRole role,
        String shiftName,
        int ratio,
        int floor
) {}