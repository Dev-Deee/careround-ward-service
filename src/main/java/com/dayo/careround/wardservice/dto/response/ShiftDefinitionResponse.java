package com.dayo.careround.wardservice.dto.response;

import java.time.LocalTime;
import java.util.UUID;

public record ShiftDefinitionResponse(
        UUID id,
        String name,
        LocalTime startTime,
        LocalTime endTime
) {}
