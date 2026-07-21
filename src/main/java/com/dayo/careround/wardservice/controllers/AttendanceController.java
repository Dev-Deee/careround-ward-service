package com.dayo.careround.wardservice.controllers;

import com.dayo.careround.wardservice.dto.request.ClockInRequest;
import com.dayo.careround.wardservice.dto.response.AttendanceResponse;
import com.dayo.careround.wardservice.security.CareRoundPrincipal;
import com.dayo.careround.wardservice.services.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponse> clockIn(
            @Valid @RequestBody ClockInRequest request,
            @AuthenticationPrincipal CareRoundPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.clockIn(principal.userId(), request));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<Void> clockOut(
            @RequestParam(defaultValue = "false") boolean overrideAuthorized,
            @AuthenticationPrincipal CareRoundPrincipal principal) {
        attendanceService.clockOut(principal.userId(), overrideAuthorized, principal.userId());
        return ResponseEntity.noContent().build();
    }
}