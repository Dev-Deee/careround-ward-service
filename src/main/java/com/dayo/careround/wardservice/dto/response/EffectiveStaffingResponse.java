package com.dayo.careround.wardservice.dto.response;

public record EffectiveStaffingResponse(
        int census,
        int effectiveRequired,
        int currentlyPresent,
        boolean coverageMet
) {}