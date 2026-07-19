package com.dayo.careround.wardservice.data.repositories;

import com.dayo.careround.wardservice.data.models.Handover;
import com.dayo.careround.wardservice.data.models.enums.HandoverStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HandoverRepository extends JpaRepository<Handover, UUID> {
    Optional<Handover> findByPublicId(UUID publicId);
    Optional<Handover> findByWardIdAndOutgoingChargeNurseIdAndStatus(UUID wardId, UUID outgoingChargeNurseId, HandoverStatus status);
}