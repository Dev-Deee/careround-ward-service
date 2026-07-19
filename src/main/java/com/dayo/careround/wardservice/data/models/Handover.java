package com.dayo.careround.wardservice.data.models;

import com.dayo.careround.wardservice.data.models.enums.HandoverStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "handovers")
public class Handover {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @Column(nullable = false)
    private UUID wardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outgoing_shift_id", nullable = false)
    private ShiftDefinition outgoingShift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incoming_shift_id", nullable = false)
    private ShiftDefinition incomingShift;

    @Column(nullable = false)
    private UUID outgoingChargeNurseId;

    @Column(nullable = false)
    private UUID incomingChargeNurseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HandoverStatus status = HandoverStatus.PENDING;

    @Column(nullable = false)
    private Instant initiatedAt = Instant.now();

    @Column
    private Instant signedOffAt;
}