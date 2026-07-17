package com.dayo.careround.wardservice.data.models;

import com.dayo.careround.wardservice.data.models.enums.AttendanceStatus;
import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendance")
@Data
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID staffId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffingRole staffRole;

    @Column(nullable = false)
    private UUID wardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="shift_definition_id",nullable = false)
    private ShiftDefinition shift;

    @Column(nullable = false)
    private Instant clockInAt;

    @Column
    private Instant clockOutAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;
}
