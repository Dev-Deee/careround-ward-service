package com.dayo.careround.wardservice.data.repositories;

import com.dayo.careround.wardservice.data.models.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WardRepository extends JpaRepository<Ward, UUID> {
    Optional<Ward>findByPublicId(UUID publicId);
}
