package com.dayo.careround.wardservice.data.repositories;

import com.dayo.careround.wardservice.data.models.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuditLogEntryRepository extends JpaRepository<AuditLogEntry, UUID> {
    Optional<AuditLogEntry> findTopByOrderByTimestampDesc();
}