package com.dayo.careround.wardservice.controllers;

import com.dayo.careround.wardservice.dto.request.GrantChargeAssignmentRequest;
import com.dayo.careround.wardservice.dto.response.ChargeAssignmentResponse;
import com.dayo.careround.wardservice.security.CareRoundPrincipal;
import com.dayo.careround.wardservice.services.ChargeAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/charge-assignments")
@RequiredArgsConstructor
public class ChargeAssignmentController {

    private final ChargeAssignmentService chargeAssignmentService;

    // ADMIN with GRANT_PERMISSIONS, or the current charge nurse transferring (rules in the service).
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('NURSE')")
    public ResponseEntity<ChargeAssignmentResponse> grant(
            @Valid @RequestBody GrantChargeAssignmentRequest request,
            @AuthenticationPrincipal CareRoundPrincipal principal,
            Authentication authentication) {
        boolean adminWithGrantPermission = authentication.getAuthorities().stream()
                .anyMatch(a -> "PERM_GRANT_PERMISSIONS".equals(a.getAuthority()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chargeAssignmentService.grant(principal, adminWithGrantPermission, request));
    }

    @GetMapping("/active")
    public ResponseEntity<ChargeAssignmentResponse> getActive(@RequestParam UUID wardId) {
        return ResponseEntity.ok(chargeAssignmentService.getActiveForWard(wardId));
    }
}