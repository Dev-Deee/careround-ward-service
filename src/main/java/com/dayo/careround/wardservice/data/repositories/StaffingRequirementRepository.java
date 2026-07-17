package com.dayo.careround.wardservice.data.repositories;

import com.dayo.careround.wardservice.data.models.StaffingRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface StaffingRequirementRepository extends JpaRepository<StaffingRequirement, UUID> {
}
