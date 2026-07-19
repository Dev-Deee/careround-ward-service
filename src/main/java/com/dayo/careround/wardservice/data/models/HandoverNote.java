package com.dayo.careround.wardservice.data.models;

import com.dayo.careround.wardservice.data.models.enums.HandoverUrgency;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "handover_notes")
public class HandoverNote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID handoverId;

    @Column(nullable = false)
    private UUID patientId; // cross-service reference, no FK

    @Column(nullable = false, columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HandoverUrgency urgency;
}