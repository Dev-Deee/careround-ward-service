package com.dayo.careround.wardservice.data.repositories;

import com.dayo.careround.wardservice.data.models.HandoverNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HandoverNoteRepository extends JpaRepository<HandoverNote, UUID> {
    List<HandoverNote> findByHandoverId(UUID handoverId);
}