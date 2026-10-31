package com.dayo.careround.wardservice.controllers;

import com.dayo.careround.wardservice.dto.request.ClockInRequest;
import com.dayo.careround.wardservice.dto.response.AttendanceResponse;
import com.dayo.careround.wardservice.security.CareRoundPrincipal;
import com.dayo.careround.wardservice.services.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // Staff id, role and ward are taken from the JWT; the body is optional.
    @PostMapping("/clock-in")
    @PreAuthorize("hasRole('NURSE') or hasRole('DOCTOR')")
    public ResponseEntity<AttendanceResponse> clockIn(
            @RequestBody(required = false) ClockInRequest request,
            @AuthenticationPrincipal CareRoundPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.clockIn(principal, request));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<Void> clockOut(
            @RequestParam(defaultValue = "false") boolean overrideAuthorized,
            @AuthenticationPrincipal CareRoundPrincipal principal) {
        attendanceService.clockOut(principal.userId(), overrideAuthorized, principal.userId());
        return ResponseEntity.noContent().build();
    }
}