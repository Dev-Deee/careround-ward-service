package com.dayo.careround.wardservice.data.repositories;

import com.dayo.careround.wardservice.data.models.Handover;
import com.dayo.careround.wardservice.data.models.enums.HandoverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HandoverRepository extends JpaRepository<Handover, UUID> {
    Optional<Handover> findByPublicId(UUID publicId);

    Page<Handover> findByWardId(UUID wardId, Pageable pageable);
    Page<Handover> findByWardIdAndStatus(UUID wardId, HandoverStatus status, Pageable pageable);
    Optional<Handover> findByWardIdAndOutgoingChargeNurseIdAndStatus(UUID wardId, UUID outgoingChargeNurseId, HandoverStatus status);
}