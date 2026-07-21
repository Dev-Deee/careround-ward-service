package com.dayo.careround.wardservice.controllers;

import com.dayo.careround.wardservice.dto.request.CreateWardRequest;
import com.dayo.careround.wardservice.dto.response.WardResponse;
import com.dayo.careround.wardservice.services.WardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardService wardService;

    @PostMapping
    public ResponseEntity<WardResponse> create(@Valid @RequestBody CreateWardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wardService.create(request));
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<WardResponse> getByPublicId(@PathVariable UUID publicId) {
        return ResponseEntity.ok(wardService.getByPublicId(publicId));
    }

    @GetMapping
    public ResponseEntity<List<WardResponse>> getAll() {
        return ResponseEntity.ok(wardService.getAll());
    }
}