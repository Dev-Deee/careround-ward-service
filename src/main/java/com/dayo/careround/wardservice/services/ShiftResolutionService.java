package com.dayo.careround.wardservice.services;

import com.dayo.careround.wardservice.data.models.ShiftDefinition;
import com.dayo.careround.wardservice.data.repositories.ShiftDefinitionRepository;
import com.dayo.careround.wardservice.exception.NoActiveShiftException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShiftResolutionService {

    private final ShiftDefinitionRepository shiftDefinitionRepository;

    public ShiftDefinition resolveCurrentShift() {
        LocalTime now = LocalTime.now();
        List<ShiftDefinition> shifts = shiftDefinitionRepository.findAll();

        return shifts.stream()
                .filter(shift -> isWithin(now, shift.getStartTime(), shift.getEndTime()))
                .findFirst()
                .orElseThrow(() -> new NoActiveShiftException(
                        "No shift definition covers the current time: " + now));
    }

    private boolean isWithin(LocalTime now, LocalTime start, LocalTime end) {
        if (start.isBefore(end)) {
            // normal same-day shift, e.g. 07:00–19:00
            return !now.isBefore(start) && now.isBefore(end);
        } else {
            // crosses midnight, e.g. 19:00–07:00
            return !now.isBefore(start) || now.isBefore(end);
        }
    }
}