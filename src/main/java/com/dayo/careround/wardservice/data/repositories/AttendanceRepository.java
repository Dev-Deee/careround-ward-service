package com.dayo.careround.wardservice.data.repositories;

import com.dayo.careround.wardservice.data.models.Attendance;
import com.dayo.careround.wardservice.data.models.enums.AttendanceStatus;
import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    List<Attendance> findByWardIdAndStatus(UUID wardId, AttendanceStatus status);

    List<Attendance> findByWardIdAndStaffRoleAndStatus(UUID wardId, StaffingRole staffRole, AttendanceStatus status);

    Optional<Attendance> findByStaffIdAndStatus(UUID staffId, AttendanceStatus status);

    Page<Attendance> findByWardId(UUID wardId, Pageable pageable);
    Page<Attendance> findByWardIdAndStatus(UUID wardId, AttendanceStatus status, Pageable pageable);
    Page<Attendance> findByWardIdAndStaffRole(UUID wardId, StaffingRole staffRole, Pageable pageable);
    Page<Attendance> findByWardIdAndStatusAndStaffRole(UUID wardId, AttendanceStatus status, StaffingRole staffRole, Pageable pageable);
}