package com.dayo.careround.wardservice.data.models;

import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import com.dayo.careround.wardservice.data.models.enums.StaffingScope;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "staffing_requirements")
public class StaffingRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffingScope scope;

    @Column
    private UUID wardId; // null when scope = HOSPITAL_DEFAULT, required when WARD_OVERRIDE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffingRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_definition_id", nullable = false)
    private ShiftDefinition shift;

    @Column(nullable = false)
    private int ratio;

    @Column(nullable = false)
    private int floor;
}