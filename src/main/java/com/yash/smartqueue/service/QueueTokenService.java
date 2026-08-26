package com.yash.smartqueue.service;

import com.yash.smartqueue.dto.TokenResponse;
import com.yash.smartqueue.model.TokenStatus;
import java.time.LocalDateTime;
import com.yash.smartqueue.model.*;
import com.yash.smartqueue.repository.DailyTokenCounterRepository;
import com.yash.smartqueue.repository.QueueTokenRepository;
import com.yash.smartqueue.websocket.WebSocketEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class QueueTokenService {

    private final QueueTokenRepository queueTokenRepository;
    private final DailyTokenCounterRepository dailyTokenCounterRepository;
    private final RedisQueueService redisQueueService;
    private final TokenScoringService tokenScoringService;
    private final WebSocketEventPublisher eventPublisher;
    private final AiPredictionService aiPredictionService;

    public QueueTokenService(
            QueueTokenRepository queueTokenRepository,
            DailyTokenCounterRepository dailyTokenCounterRepository,
            RedisQueueService redisQueueService,
            TokenScoringService tokenScoringService,
            WebSocketEventPublisher eventPublisher,
            AiPredictionService aiPredictionService) {

        this.queueTokenRepository = queueTokenRepository;
        this.dailyTokenCounterRepository = dailyTokenCounterRepository;
        this.redisQueueService = redisQueueService;
        this.tokenScoringService = tokenScoringService;
        this.eventPublisher = eventPublisher;
        this.aiPredictionService = aiPredictionService;
    }

    @Transactional
    public QueueToken createToken(
            ServiceType serviceType,
            String customerName,
            String customerPhone) {

        LocalDate today = LocalDate.now();

        // 1. Lock today's counter row for this service type
        DailyTokenCounter counter = dailyTokenCounterRepository
                .findForUpdate(serviceType, today)
                .orElseGet(() -> {
                    DailyTokenCounter newCounter = new DailyTokenCounter();
                    newCounter.setServiceType(serviceType);
                    newCounter.setCounterDate(today);
                    newCounter.setLastNumber(0);
                    return newCounter;
                });

        // 2. Generate next token number
        int nextNumber = counter.getLastNumber() + 1;
        counter.setLastNumber(nextNumber);

        dailyTokenCounterRepository.save(counter);

        // 3. Create token
        QueueToken token = new QueueToken();

        token.setTokenNumber(nextNumber);
        token.setServiceType(serviceType);
        token.setCustomerName(customerName);
        token.setCustomerPhone(customerPhone);
        token.setPriority(Priority.NORMAL);
        token.setStatus(TokenStatus.WAITING);

        // 4. Save token in MySQL
        QueueToken saved = queueTokenRepository.save(token);

        // 5. Mirror waiting token into Redis
        double score = tokenScoringService.computeScore(saved);

        redisQueueService.addTokenToQueue(saved, score);
        // Broadcasting to all connected clients
        eventPublisher.publishTokenCreated(saved);

        return saved;

    }

    @Transactional
    public QueueToken completeToken(Long tokenId) {
        QueueToken token = queueTokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));

        if (token.getStatus() != TokenStatus.SERVING) {
            throw new IllegalStateException("Only SERVING tokens can be completed");
        }

        token.setStatus(TokenStatus.COMPLETED);
        token.setCompletedAt(LocalDateTime.now());

        QueueToken saved = queueTokenRepository.save(token);

        if (token.getCounter() != null) {
            redisQueueService.removeFromServing(token.getCounter().getId());
        }
        eventPublisher.publishTokenCompleted(saved);

        return saved;
    }

    @Transactional
    public QueueToken skipToken(Long tokenId) {
        QueueToken token = queueTokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));

        if (token.getStatus() != TokenStatus.WAITING) {
            throw new IllegalStateException("Only WAITING tokens can be skipped");
        }

        token.setStatus(TokenStatus.SKIPPED);
        QueueToken saved = queueTokenRepository.save(token);

        redisQueueService.removeFromQueue(
                saved.getId(),
                saved.getServiceType().getPrefix()
        );
        eventPublisher.publishTokenSkipped(saved);

        return saved;
    }
    @Transactional
    public Map<String, Object> createTokenWithPrediction(
            ServiceType serviceType,
            String customerName,
            String customerPhone) {

        // Create token as before
        QueueToken saved = createToken(serviceType, customerName, customerPhone);

        // Get current queue stats for AI features
        long queueLength = redisQueueService.getQueueLength(
                serviceType.getPrefix());
        double avgServiceMinutes = serviceType.getAvgServiceMinutes() != null
                ? serviceType.getAvgServiceMinutes() : 7.0;

        // Estimate priority ratio from recent tokens (simplified)
        double priorityRatio = 0.1; // default — will improve with real data

        // Call AI service
        double predictedWait = aiPredictionService.predictWaitTime(
                serviceType.getPrefix(),
                avgServiceMinutes,
                priorityRatio
        );

        // Build enriched response
        Map<String, Object> result = new HashMap<>();
        result.put("token", TokenResponse.fromEntity(saved, predictedWait));
        result.put("predictedWaitMinutes",
                predictedWait > 0 ? predictedWait : null);
        result.put("queuePosition", queueLength);

        return result;
    }
}