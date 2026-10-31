package com.dayo.careround.wardservice.controllers;

import com.dayo.careround.wardservice.dto.request.CreateWardRequest;
import com.dayo.careround.wardservice.dto.response.WardResponse;
import com.dayo.careround.wardservice.services.WardService;
import jakarta.validation.Valid;
import com.dayo.careround.wardservice.services.WardReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardService wardService;
    private final WardReadService wardReadService;

    // Hospital configuration: admin with MANAGE_SYSTEM_CONFIG.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_MANAGE_SYSTEM_CONFIG')")
    public ResponseEntity<WardResponse> create(@Valid @RequestBody CreateWardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wardService.create(request));
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<WardResponse> getByPublicId(@PathVariable UUID publicId) {
        return ResponseEntity.ok(wardService.getByPublicId(publicId));
    }

    // With page or size: {items, page, size, total}, sorted by name. Without either: the legacy bare array
    // (deprecated; kept for existing callers).
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) Integer page,
                                    @RequestParam(required = false) Integer size) {
        if (page == null && size == null) return ResponseEntity.ok(wardService.getAll());
        return ResponseEntity.ok(wardReadService.wards(page, size));
    }
}