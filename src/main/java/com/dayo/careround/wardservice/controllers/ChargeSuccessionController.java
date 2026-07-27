package com.dayo.careround.wardservice.controllers;

import com.dayo.careround.wardservice.dto.request.ClaimChargeRequest;
import com.dayo.careround.wardservice.security.CareRoundPrincipal;
import com.dayo.careround.wardservice.services.ChargeSuccessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/charge-succession")
@RequiredArgsConstructor
public class ChargeSuccessionController {

    private final ChargeSuccessionService chargeSuccessionService;

    @PostMapping("/claim")
    public ResponseEntity<Void> claim(
            @Valid @RequestBody ClaimChargeRequest request,
            @AuthenticationPrincipal CareRoundPrincipal principal) {
        chargeSuccessionService.claim(request.wardId(), principal.userId());
        return ResponseEntity.noContent().build();
    }
}