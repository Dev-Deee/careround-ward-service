package com.dayo.careround.wardservice.dto.response;

import com.dayo.careround.wardservice.data.models.enums.AttendanceStatus;

import java.time.Instant;
import java.util.UUID;

public record AttendanceResponse(
        UUID id,
        UUID staffId,
        UUID wardId,
        String shiftName,
        Instant clockInAt,
        Instant clockOutAt,
        AttendanceStatus status
) {}