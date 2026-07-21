package com.dayo.careround.wardservice.utils;

import com.dayo.careround.wardservice.data.models.ShiftDefinition;
import com.dayo.careround.wardservice.data.models.Ward;
import com.dayo.careround.wardservice.dto.request.CreateShiftDefinitionRequest;
import com.dayo.careround.wardservice.dto.request.CreateWardRequest;
import com.dayo.careround.wardservice.dto.response.ShiftDefinitionResponse;
import com.dayo.careround.wardservice.dto.response.WardResponse;

public class WardMapper {

    private WardMapper() {
    }

    public static Ward toWard(CreateWardRequest request) {
        Ward ward = new Ward();
        ward.setName(request.name());
        ward.setCapacity(request.capacity());
        return ward;
    }

    public static WardResponse toWardResponse(Ward ward) {
        return new WardResponse(ward.getPublicId(), ward.getName(), ward.getCapacity());
    }

    public static ShiftDefinition toShiftDefinition(CreateShiftDefinitionRequest request) {
        ShiftDefinition shift = new ShiftDefinition();
        shift.setName(request.name());
        shift.setStartTime(request.startTime());
        shift.setEndTime(request.endTime());
        return shift;
    }

    public static ShiftDefinitionResponse toShiftDefinitionResponse(ShiftDefinition shift) {
        return new ShiftDefinitionResponse(shift.getId(), shift.getName(), shift.getStartTime(), shift.getEndTime());
    }
}