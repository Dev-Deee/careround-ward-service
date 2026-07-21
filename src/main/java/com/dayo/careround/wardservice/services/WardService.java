package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.Ward;
import com.dayo.careround.wardservice.data.repositories.WardRepository;
import com.dayo.careround.wardservice.dto.request.CreateWardRequest;
import com.dayo.careround.wardservice.dto.response.WardResponse;
import com.dayo.careround.wardservice.exception.WardNotFoundException;
import com.dayo.careround.wardservice.utils.WardMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WardService {

    private final WardRepository wardRepository;

    @Transactional
    public WardResponse create(CreateWardRequest request) {
        Ward ward = WardMapper.toWard(request);
        wardRepository.save(ward);
        return WardMapper.toWardResponse(ward);
    }

    public WardResponse getByPublicId(UUID publicId) {
        Ward ward = wardRepository.findByPublicId(publicId)
                .orElseThrow(() -> new WardNotFoundException("No ward with that public id"));
        return WardMapper.toWardResponse(ward);
    }

    public List<WardResponse> getAll() {
        return wardRepository.findAll().stream()
                .map(WardMapper::toWardResponse)
                .toList();
    }
}