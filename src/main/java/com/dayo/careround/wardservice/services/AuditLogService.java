package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.AuditLogEntry;
import com.dayo.careround.wardservice.data.repositories.AuditLogEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogEntryRepository auditLogEntryRepository;

    public void log(UUID actorId, String actorRole, String action,
                    String resourceType, UUID resourceId, UUID wardContext, String reason) {

        String previousHash = auditLogEntryRepository.findTopByOrderByTimestampDesc()
                .map(AuditLogEntry::getEntryHash)
                .orElse(null);

        AuditLogEntry entry = new AuditLogEntry();
        entry.setTimestamp(Instant.now());
        entry.setActorId(actorId);
        entry.setActorRole(actorRole);
        entry.setAction(action);
        entry.setResourceType(resourceType);
        entry.setResourceId(resourceId);
        entry.setWardContext(wardContext);
        entry.setReason(reason);
        entry.setPreviousHash(previousHash);
        entry.setEntryHash(computeHash(entry));

        auditLogEntryRepository.save(entry);
    }

    private String computeHash(AuditLogEntry entry) {
        try {
            String payload = String.join("|",
                    String.valueOf(entry.getTimestamp()),
                    String.valueOf(entry.getActorId()),
                    entry.getActorRole(),
                    entry.getAction(),
                    entry.getResourceType(),
                    String.valueOf(entry.getResourceId()),
                    String.valueOf(entry.getWardContext()),
                    String.valueOf(entry.getReason()),
                    String.valueOf(entry.getPreviousHash())
            );
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute audit log hash", e);
        }
    }
}