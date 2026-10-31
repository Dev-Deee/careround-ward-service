package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.Handover;
import com.dayo.careround.wardservice.data.models.HandoverNote;
import com.dayo.careround.wardservice.data.models.enums.AttendanceStatus;
import com.dayo.careround.wardservice.data.models.enums.ChargeAssignmentStatus;
import com.dayo.careround.wardservice.data.models.enums.HandoverStatus;
import com.dayo.careround.wardservice.data.models.enums.StaffingRole;
import com.dayo.careround.wardservice.data.repositories.AttendanceRepository;
import com.dayo.careround.wardservice.data.repositories.ChargeAssignmentRepository;
import com.dayo.careround.wardservice.data.repositories.HandoverNoteRepository;
import com.dayo.careround.wardservice.data.repositories.HandoverRepository;
import com.dayo.careround.wardservice.data.repositories.WardRepository;
import com.dayo.careround.wardservice.dto.response.AttendanceResponse;
import com.dayo.careround.wardservice.dto.response.HandoverDetailResponse;
import com.dayo.careround.wardservice.dto.response.HandoverResponse;
import com.dayo.careround.wardservice.dto.response.PageResponse;
import com.dayo.careround.wardservice.dto.response.WardResponse;
import com.dayo.careround.wardservice.exception.HandoverNotFoundException;
import com.dayo.careround.wardservice.exception.WardNotFoundException;
import com.dayo.careround.wardservice.security.CareRoundPrincipal;
import com.dayo.careround.wardservice.utils.WardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.UUID;

// Ward-scoped read side. Attendance and handover lists are supervision views: ADMIN, or the ward's active charge
// nurse. A single handover is also visible to the two nurses named on it.
@Service
@RequiredArgsConstructor
public class WardReadService {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private final WardRepository wardRepository;
    private final AttendanceRepository attendanceRepository;
    private final HandoverRepository handoverRepository;
    private final HandoverNoteRepository handoverNoteRepository;
    private final ChargeAssignmentRepository chargeAssignmentRepository;

    @Transactional(readOnly = true)
    public PageResponse<WardResponse> wards(Integer page, Integer size) {
        return PageResponse.of(wardRepository.findAll(pageable(page, size,
                Sort.by(Sort.Order.asc("name").ignoreCase(), Sort.Order.asc("id")))), WardMapper::toWardResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<AttendanceResponse> attendance(CareRoundPrincipal caller, UUID wardId, AttendanceStatus status,
                                                       StaffingRole role, Integer page, Integer size) {
        requireSupervisor(caller, wardId);
        Pageable p = pageable(page, size, Sort.by(Sort.Order.desc("clockInAt"), Sort.Order.asc("id")));
        var rows = status != null && role != null ? attendanceRepository.findByWardIdAndStatusAndStaffRole(wardId, status, role, p)
                : status != null ? attendanceRepository.findByWardIdAndStatus(wardId, status, p)
                : role != null ? attendanceRepository.findByWardIdAndStaffRole(wardId, role, p)
                : attendanceRepository.findByWardId(wardId, p);
        return PageResponse.of(rows, a -> new AttendanceResponse(a.getId(), a.getStaffId(), a.getWardId(),
                a.getShift().getName(), a.getClockInAt(), a.getClockOutAt(), a.getStatus(), a.getStaffRole()));
    }

    @Transactional(readOnly = true)
    public PageResponse<HandoverResponse> handovers(CareRoundPrincipal caller, UUID wardId, HandoverStatus status,
                                                    Integer page, Integer size) {
        requireSupervisor(caller, wardId);
        Pageable p = pageable(page, size, Sort.by(Sort.Order.desc("initiatedAt"), Sort.Order.asc("id")));
        var rows = status != null ? handoverRepository.findByWardIdAndStatus(wardId, status, p)
                : handoverRepository.findByWardId(wardId, p);
        return PageResponse.of(rows, h -> new HandoverResponse(h.getPublicId(), h.getWardId(), h.getOutgoingChargeNurseId(),
                h.getIncomingChargeNurseId(), h.getStatus(), h.getInitiatedAt(), h.getSignedOffAt()));
    }

    @Transactional(readOnly = true)
    public HandoverDetailResponse handover(CareRoundPrincipal caller, UUID publicId) {
        Handover h = handoverRepository.findByPublicId(publicId)
                .orElseThrow(() -> new HandoverNotFoundException("No handover with that id"));
        boolean named = caller.userId().equals(h.getOutgoingChargeNurseId()) || caller.userId().equals(h.getIncomingChargeNurseId());
        if (!named) requireSupervisor(caller, h.getWardId());
        var notes = handoverNoteRepository.findByHandoverId(h.getId()).stream()
                .sorted(Comparator.comparing(HandoverNote::getUrgency).reversed() // URGENT first
                        .thenComparing(HandoverNote::getId))
                .map(n -> new HandoverDetailResponse.Note(n.getId(), n.getPatientId(), n.getNote(), n.getUrgency()))
                .toList();
        return new HandoverDetailResponse(h.getPublicId(), h.getWardId(), h.getOutgoingChargeNurseId(),
                h.getIncomingChargeNurseId(), h.getOutgoingShift().getName(), h.getIncomingShift().getName(),
                h.getStatus(), h.getInitiatedAt(), h.getSignedOffAt(), notes);
    }

    private void requireSupervisor(CareRoundPrincipal caller, UUID wardId) {
        if (wardRepository.findByPublicId(wardId).isEmpty()) throw new WardNotFoundException("No ward with that id");
        if ("ADMIN".equals(caller.role())) return;
        boolean charge = "NURSE".equals(caller.role()) && chargeAssignmentRepository
                .findByWardIdAndStatus(wardId, ChargeAssignmentStatus.ACTIVE)
                .map(ca -> ca.getNurseId().equals(caller.userId())).orElse(false);
        if (!charge) throw new AccessDeniedException("Only the ward's active charge nurse or an admin can view this");
    }

    private static Pageable pageable(Integer page, Integer size, Sort sort) {
        return PageRequest.of(page == null ? 0 : Math.max(page, 0),
                size == null ? DEFAULT_SIZE : Math.min(Math.max(size, 1), MAX_SIZE), sort);
    }
}
