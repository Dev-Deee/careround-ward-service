package com.dayo.careround.wardservice.data.models;

import com.dayo.careround.wardservice.data.models.enums.SuccessionStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "charge_succession_windows")
public class ChargeSuccessionWindow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID wardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_definition_id", nullable = false)
    private ShiftDefinition shift;

    @Column(nullable = false)
    private UUID vacatedNurseId; // the charge nurse who didn't show up

    @Column(nullable = false)
    private Instant openedAt;

    @Column(nullable = false)
    private Instant claimDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SuccessionStatus status;

    @Column
    private UUID claimedByNurseId; // set once resolved, whether claimed or auto-assigned
}