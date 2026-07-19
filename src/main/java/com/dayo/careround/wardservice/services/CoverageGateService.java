package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.StaffingRequirement;
import com.dayo.careround.wardservice.data.models.enums.AttendanceStatus;
import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import com.dayo.careround.wardservice.data.repositories.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoverageGateService {
    private final AttendanceRepository attendanceRepository;
    private final StaffingRequirementService staffingRequirementService;
    private final PatientCensusClient patientCensusClient;

    public boolean wouldBreachCoverage(UUID wardId, StaffingRole role, UUID shiftId, UUID staffIdClockingOut) {
        int currentlyPresent = attendanceRepository
                .findByWardIdAndStaffRoleAndStatus(wardId, role, AttendanceStatus.CLOCKED_IN)
                .size();

        int presentAfterClockOut = currentlyPresent - 1;

        int census = patientCensusClient.getAdmittedCount(wardId);
        StaffingRequirement requirement = staffingRequirementService.resolve(wardId, role, shiftId);

        int effectiveRequired = Math.max(
                requirement.getFloor(),
                (int) Math.ceil((double) census / requirement.getRatio())
        );

        return presentAfterClockOut < effectiveRequired;
    }
}
