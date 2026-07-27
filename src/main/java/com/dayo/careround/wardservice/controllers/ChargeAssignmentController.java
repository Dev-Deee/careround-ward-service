package com.dayo.careround.wardservice.controllers;

import com.dayo.careround.wardservice.dto.request.GrantChargeAssignmentRequest;
import com.dayo.careround.wardservice.dto.response.ChargeAssignmentResponse;
import com.dayo.careround.wardservice.security.CareRoundPrincipal;
import com.dayo.careround.wardservice.services.ChargeAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/charge-assignments")
@RequiredArgsConstructor
public class ChargeAssignmentController {

    private final ChargeAssignmentService chargeAssignmentService;

    @PostMapping
    public ResponseEntity<ChargeAssignmentResponse> grant(
            @Valid @RequestBody GrantChargeAssignmentRequest request,
            @AuthenticationPrincipal CareRoundPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chargeAssignmentService.grant(principal.userId(), request));
    }

    @GetMapping("/active")
    public ResponseEntity<ChargeAssignmentResponse> getActive(@RequestParam UUID wardId) {
        return ResponseEntity.ok(chargeAssignmentService.getActiveForWard(wardId));
    }
}