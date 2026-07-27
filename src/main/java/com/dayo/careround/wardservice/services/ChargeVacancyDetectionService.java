package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.ChargeAssignment;
import com.dayo.careround.wardservice.data.models.ShiftDefinition;
import com.dayo.careround.wardservice.data.models.enums.AttendanceStatus;
import com.dayo.careround.wardservice.data.models.enums.ChargeAssignmentStatus;
import com.dayo.careround.wardservice.data.repositories.AttendanceRepository;
import com.dayo.careround.wardservice.data.repositories.ChargeAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargeVacancyDetectionService {

    private final ChargeAssignmentRepository chargeAssignmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final ShiftResolutionService shiftResolutionService;
    private final ChargeSuccessionService chargeSuccessionService;

    public void checkForVacancies() {
        ShiftDefinition currentShift = shiftResolutionService.resolveCurrentShift();

        List<ChargeAssignment> activeAssignments = chargeAssignmentRepository
                .findAll().stream()
                .filter(ca -> ca.getStatus() == ChargeAssignmentStatus.ACTIVE)
                .filter(ca -> ca.getShift().getId().equals(currentShift.getId()))
                .toList();

        for (ChargeAssignment assignment : activeAssignments) {
            boolean clockedIn = attendanceRepository
                    .findByStaffIdAndStatus(assignment.getNurseId(), AttendanceStatus.CLOCKED_IN)
                    .filter(a -> a.getWardId().equals(assignment.getWardId()))
                    .isPresent();

            if (!clockedIn) {
                log.warn("Charge nurse {} has not clocked in for ward {} — opening succession window",
                        assignment.getNurseId(), assignment.getWardId());
                chargeSuccessionService.openSuccessionWindow(
                        assignment.getWardId(), currentShift, assignment.getNurseId());
            }
        }
    }
}