package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.StaffingRequirement;
import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StaffingRequirementService {
    public StaffingRequirement resolve(UUID wardId, StaffingRole role, UUID shiftId) {
        return null;
    }
}
