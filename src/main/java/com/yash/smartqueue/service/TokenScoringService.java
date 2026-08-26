package com.yash.smartqueue.service;

import com.yash.smartqueue.model.Priority;
import com.yash.smartqueue.model.QueueToken;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class TokenScoringService {

    private static final double EMERGENCY_WEIGHT = 1000.0;
    private static final double PRIORITY_WEIGHT = 500.0;
    private static final double NORMAL_WEIGHT = 0.0;
    private static final double AGING_FACTOR_PER_MINUTE = 2.0;

    public double computeScore(QueueToken token) {
        double baseWeight = switch (token.getPriority()) {
            case EMERGENCY -> EMERGENCY_WEIGHT;
            case PRIORITY -> PRIORITY_WEIGHT;
            case NORMAL -> NORMAL_WEIGHT;
        };

        long minutesWaited = Duration.between(token.getCreatedAt(), LocalDateTime.now()).toMinutes();
        double agingBonus = minutesWaited * AGING_FACTOR_PER_MINUTE;

        return baseWeight + agingBonus;
    }
}