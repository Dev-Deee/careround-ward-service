package com.dayo.careround.wardservice.controllers;

import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import com.dayo.careround.wardservice.dto.request.CreateStaffingRequirementRequest;
import com.dayo.careround.wardservice.dto.response.EffectiveStaffingResponse;
import com.dayo.careround.wardservice.dto.response.StaffingRequirementResponse;
import com.dayo.careround.wardservice.services.StaffingRequirementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/staffing-requirements")
@RequiredArgsConstructor
public class StaffingRequirementController {

    private final StaffingRequirementService staffingRequirementService;

    // Hospital configuration: admin with MANAGE_SYSTEM_CONFIG.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_MANAGE_SYSTEM_CONFIG')")
    public ResponseEntity<StaffingRequirementResponse> create(@Valid @RequestBody CreateStaffingRequirementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffingRequirementService.create(request));
    }

    @GetMapping("/effective")
    public ResponseEntity<EffectiveStaffingResponse> getEffective(
            @RequestParam UUID wardId,
            @RequestParam StaffingRole role,
            @RequestParam UUID shiftId) {
        return ResponseEntity.ok(staffingRequirementService.getEffectiveStaffing(wardId, role, shiftId));
    }
}