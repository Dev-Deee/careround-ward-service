package com.dayo.careround.wardservice.controllers;

import com.dayo.careround.wardservice.dto.request.CreateShiftDefinitionRequest;
import com.dayo.careround.wardservice.dto.response.ShiftDefinitionResponse;
import com.dayo.careround.wardservice.services.ShiftDefinitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shift-definitions")
@RequiredArgsConstructor
public class ShiftDefinitionController {

    private final ShiftDefinitionService shiftDefinitionService;

    // Hospital configuration: admin with MANAGE_SYSTEM_CONFIG.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_MANAGE_SYSTEM_CONFIG')")
    public ResponseEntity<ShiftDefinitionResponse> create(@Valid @RequestBody CreateShiftDefinitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shiftDefinitionService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ShiftDefinitionResponse>> getAll() {
        return ResponseEntity.ok(shiftDefinitionService.getAll());
    }
}