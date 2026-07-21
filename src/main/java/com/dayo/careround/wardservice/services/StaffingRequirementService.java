package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.StaffingRequirement;
import com.dayo.careround.wardservice.data.models.enums.AttendanceStatus;
import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import com.dayo.careround.wardservice.data.models.enums.StaffingScope;
import com.dayo.careround.wardservice.data.repositories.AttendanceRepository;
import com.dayo.careround.wardservice.data.repositories.ShiftDefinitionRepository;
import com.dayo.careround.wardservice.data.repositories.StaffingRequirementRepository;
import com.dayo.careround.wardservice.dto.request.CreateStaffingRequirementRequest;
import com.dayo.careround.wardservice.dto.response.EffectiveStaffingResponse;
import com.dayo.careround.wardservice.dto.response.StaffingRequirementResponse;
import com.dayo.careround.wardservice.exception.InvalidStaffingRequirementException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffingRequirementService {

    private final StaffingRequirementRepository staffingRequirementRepository;
    private final ShiftDefinitionRepository shiftDefinitionRepository;
    private final AttendanceRepository attendanceRepository;
    private final PatientCensusClient patientCensusClient;

    @Transactional
    public StaffingRequirementResponse create(CreateStaffingRequirementRequest request) {
        if (request.scope() == StaffingScope.WARD_OVERRIDE && request.wardId() == null) {
            throw new InvalidStaffingRequirementException("wardId is required for a WARD_OVERRIDE requirement");
        }
        if (request.scope() == StaffingScope.HOSPITAL_DEFAULT && request.wardId() != null) {
            throw new InvalidStaffingRequirementException("wardId must be null for a HOSPITAL_DEFAULT requirement");
        }

        StaffingRequirement requirement = new StaffingRequirement();
        requirement.setScope(request.scope());
        requirement.setWardId(request.wardId());
        requirement.setRole(request.role());
        requirement.setShift(shiftDefinitionRepository.findById(request.shiftId())
                .orElseThrow(() -> new InvalidStaffingRequirementException("No shift with that id")));
        requirement.setRatio(request.ratio());
        requirement.setFloor(request.floor());

        staffingRequirementRepository.save(requirement);
        return toResponse(requirement);
    }

    public StaffingRequirement resolve(UUID wardId, StaffingRole role, UUID shiftId) {
        return staffingRequirementRepository
                .findByScopeAndWardIdAndRoleAndShiftId(StaffingScope.WARD_OVERRIDE, wardId, role, shiftId)
                .orElseGet(() -> staffingRequirementRepository
                        .findByScopeAndRoleAndShiftId(StaffingScope.HOSPITAL_DEFAULT, role, shiftId)
                        .orElseThrow(() -> new InvalidStaffingRequirementException(
                                "No staffing requirement configured for role=" + role + ", shift=" + shiftId)));
    }

    public EffectiveStaffingResponse getEffectiveStaffing(UUID wardId, StaffingRole role, UUID shiftId) {
        StaffingRequirement requirement = resolve(wardId, role, shiftId);

        int census = patientCensusClient.getAdmittedCount(wardId);
        int effectiveRequired = Math.max(requirement.getFloor(),
                (int) Math.ceil((double) census / requirement.getRatio()));

        int currentlyPresent = attendanceRepository
                .findByWardIdAndStaffRoleAndStatus(wardId, role, AttendanceStatus.CLOCKED_IN)
                .size();

        return new EffectiveStaffingResponse(census, effectiveRequired, currentlyPresent,
                currentlyPresent >= effectiveRequired);
    }

    private StaffingRequirementResponse toResponse(StaffingRequirement requirement) {
        return new StaffingRequirementResponse(
                requirement.getId(), requirement.getScope(), requirement.getWardId(),
                requirement.getRole(), requirement.getShift().getName(),
                requirement.getRatio(), requirement.getFloor()
        );
    }
}