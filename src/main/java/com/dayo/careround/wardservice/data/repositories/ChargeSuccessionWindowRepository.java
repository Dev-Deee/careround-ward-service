package com.dayo.careround.wardservice.data.repositories;

import com.dayo.careround.wardservice.data.models.ChargeSuccessionWindow;
import com.dayo.careround.wardservice.data.models.enums.SuccessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChargeSuccessionWindowRepository extends JpaRepository<ChargeSuccessionWindow, UUID> {
    Optional<ChargeSuccessionWindow> findByWardIdAndStatus(UUID wardId, SuccessionStatus status);
    List<ChargeSuccessionWindow> findByStatusAndClaimDeadlineBefore(SuccessionStatus status, java.time.Instant now);
}