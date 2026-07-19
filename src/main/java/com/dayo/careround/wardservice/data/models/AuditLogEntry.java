package com.dayo.careround.wardservice.data.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "audit_log_entries")
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private UUID actorId;

    @Column(nullable = false)
    private String actorRole;

    @Column(nullable = false)
    private String action; // e.g. "COVERAGE_GATE_OVERRIDE", "CHARGE_REASSIGNED"

    @Column(nullable = false)
    private String resourceType; // e.g. "Attendance", "ChargeAssignment"

    @Column(nullable = false)
    private UUID resourceId;

    @Column
    private UUID wardContext;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column
    private String previousHash; // null for the very first entry in the chain

    @Column(nullable = false, unique = true)
    private String entryHash;
}