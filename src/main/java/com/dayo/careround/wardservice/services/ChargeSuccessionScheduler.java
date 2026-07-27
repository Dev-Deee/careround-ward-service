package com.dayo.careround.wardservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChargeSuccessionScheduler {

    private final ChargeSuccessionService chargeSuccessionService;
    private final ChargeVacancyDetectionService vacancyDetectionService;

    @Scheduled(fixedDelay = 60000)
    public void detectVacancies() {
        vacancyDetectionService.checkForVacancies();
    }

    @Scheduled(fixedDelay = 60000) // check every 60 seconds
    public void sweep() {
        chargeSuccessionService.resolveExpiredWindows();
    }
}