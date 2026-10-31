package com.dayo.careround.wardservice.dto.request;

import com.dayo.careround.wardservice.data.models.enums.StaffingRole;

import java.util.UUID;

// Who and where come from the JWT (role, assignedWardId). Both fields are optional and, when sent, must match
// the token; they are kept only so existing clients that still send them keep working.
public record ClockInRequest(
        UUID wardId,
        StaffingRole staffRole
) {}
