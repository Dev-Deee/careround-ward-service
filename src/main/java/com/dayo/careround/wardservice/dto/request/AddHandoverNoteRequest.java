package com.dayo.careround.wardservice.dto.request;

import com.dayo.careround.wardservice.data.models.enums.HandoverUrgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddHandoverNoteRequest(
        @NotNull UUID patientId,
        @NotBlank String note,
        @NotNull HandoverUrgency urgency
) {}