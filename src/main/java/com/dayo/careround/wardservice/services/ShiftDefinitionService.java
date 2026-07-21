package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.ShiftDefinition;
import com.dayo.careround.wardservice.data.repositories.ShiftDefinitionRepository;
import com.dayo.careround.wardservice.dto.request.CreateShiftDefinitionRequest;
import com.dayo.careround.wardservice.dto.response.ShiftDefinitionResponse;
import com.dayo.careround.wardservice.utils.WardMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftDefinitionService {

    private final ShiftDefinitionRepository shiftDefinitionRepository;

    @Transactional
    public ShiftDefinitionResponse create(CreateShiftDefinitionRequest request) {
        ShiftDefinition shift = WardMapper.toShiftDefinition(request);
        shiftDefinitionRepository.save(shift);
        return WardMapper.toShiftDefinitionResponse(shift);
    }

    public List<ShiftDefinitionResponse> getAll() {
        return shiftDefinitionRepository.findAll().stream()
                .map(WardMapper::toShiftDefinitionResponse)
                .toList();
    }
}