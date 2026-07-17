package com.dayo.careround.wardservice.data.repositories;

import com.dayo.careround.wardservice.data.models.ShiftDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShiftDefinitionRepository extends JpaRepository<ShiftDefinition, UUID> {

}