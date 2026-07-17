package com.dayo.careround.wardservice.data.repositories;

import com.dayo.careround.wardservice.data.models.ChargeAssignment;
import com.dayo.careround.wardservice.data.models.enums.ChargeAssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChargeAssignmentRepository extends JpaRepository<ChargeAssignment, UUID> {

    Optional<ChargeAssignment> findByWardIdAndStatus(UUID wardId, ChargeAssignmentStatus status);
}