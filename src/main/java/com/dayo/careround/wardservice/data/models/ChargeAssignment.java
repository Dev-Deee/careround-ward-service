package com.dayo.careround.wardservice.data.models;

import com.dayo.careround.wardservice.data.models.enums.ChargeAssignmentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "charge_assignments")
public class ChargeAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID wardId;

    @Column(nullable = false)
    private UUID nurseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_definition_id", nullable = false)
    private ShiftDefinition shift;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChargeAssignmentStatus status;

    @Column(nullable = false)
    private UUID grantedBy; // who granted this — admin, or auto-succession logic

    @Column(nullable = false)
    private Instant grantedAt;

    @Column
    private Instant expiresAt;
}