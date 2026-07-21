package com.dayo.careround.wardservice.data.repositories;

import com.dayo.careround.wardservice.data.models.StaffingRequirement;
import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import com.dayo.careround.wardservice.data.models.enums.StaffingScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StaffingRequirementRepository extends JpaRepository<StaffingRequirement, UUID> {

    Optional<StaffingRequirement> findByScopeAndWardIdAndRoleAndShiftId(
            StaffingScope scope, UUID wardId, StaffingRole role, UUID shiftId);

    Optional<StaffingRequirement> findByScopeAndRoleAndShiftId(
            StaffingScope scope, StaffingRole role, UUID shiftId);
}