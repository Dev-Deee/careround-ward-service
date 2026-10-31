package com.dayo.careround.wardservice.controllers;

import com.dayo.careround.wardservice.dto.request.ClockInRequest;
import com.dayo.careround.wardservice.dto.request.OverrideClockOutRequest;
import com.dayo.careround.wardservice.dto.response.AttendanceResponse;
import com.dayo.careround.wardservice.security.CareRoundPrincipal;
import com.dayo.careround.wardservice.services.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    // The caller's own clock-out. There is no self-override any more: see /override-clock-out.
    @PostMapping("/clock-out")
    @PreAuthorize("hasRole('NURSE') or hasRole('DOCTOR')")
    public ResponseEntity<Void> clockOut(@AuthenticationPrincipal CareRoundPrincipal principal) {
        attendanceService.clockOut(principal.userId());
        return ResponseEntity.noContent().build();
    }

    // Authorised override of another staff member's gated clock-out: charge holder of that ward or ADMIN with
    // MANAGE_SYSTEM_CONFIG (checked in the service, which knows the attendance's ward).
    @PostMapping("/override-clock-out")
    @PreAuthorize("hasRole('NURSE') or hasRole('ADMIN')")
    public ResponseEntity<Void> overrideClockOut(
            @Valid @RequestBody OverrideClockOutRequest request,
            @AuthenticationPrincipal CareRoundPrincipal principal,
            Authentication authentication) {
        boolean adminWithConfigPermission = authentication.getAuthorities().stream()
                .anyMatch(a -> "PERM_MANAGE_SYSTEM_CONFIG".equals(a.getAuthority()));
        attendanceService.overrideClockOut(principal, adminWithConfigPermission, request.staffId(), request.reason());
        return ResponseEntity.noContent().build();
    }
}