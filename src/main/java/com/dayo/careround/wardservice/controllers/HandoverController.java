package com.dayo.careround.wardservice.controllers;

import com.dayo.careround.wardservice.data.models.enums.HandoverUrgency;
import com.dayo.careround.wardservice.dto.request.AddHandoverNoteRequest;
import com.dayo.careround.wardservice.dto.request.InitiateHandoverRequest;
import com.dayo.careround.wardservice.dto.response.HandoverResponse;
import com.dayo.careround.wardservice.security.CareRoundPrincipal;
import com.dayo.careround.wardservice.services.HandoverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/handovers")
@RequiredArgsConstructor
public class HandoverController {

    private final HandoverService handoverService;

    @PostMapping
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<HandoverResponse> initiate(
            @Valid @RequestBody InitiateHandoverRequest request,
            @AuthenticationPrincipal CareRoundPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(handoverService.initiate(request.wardId(), principal.userId(), request));
    }

    @PostMapping("/{handoverId}/notes")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<Void> addNote(
            @PathVariable UUID handoverId,
            @Valid @RequestBody AddHandoverNoteRequest request,
            @AuthenticationPrincipal CareRoundPrincipal principal) {
        handoverService.addNote(handoverId, principal.userId(), request.patientId(), request.note(), request.urgency());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{handoverId}/sign-off")
    @PreAuthorize("hasRole('NURSE')")
    public ResponseEntity<Void> signOff(@PathVariable UUID handoverId,
                                        @AuthenticationPrincipal CareRoundPrincipal principal) {
        handoverService.signOff(handoverId, principal.userId());
        return ResponseEntity.noContent().build();
    }
}