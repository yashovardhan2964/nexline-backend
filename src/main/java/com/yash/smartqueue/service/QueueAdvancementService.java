package com.yash.smartqueue.service;

import com.yash.smartqueue.model.*;
import com.yash.smartqueue.repository.CounterRepository;
import com.yash.smartqueue.repository.QueueTokenRepository;
import com.yash.smartqueue.websocket.WebSocketEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class QueueAdvancementService {

    private final QueueTokenRepository queueTokenRepository;
    private final CounterRepository counterRepository;
    private final TokenScoringService tokenScoringService;
    private final RedisQueueService redisQueueService;
    private final WebSocketEventPublisher eventPublisher;

    public QueueAdvancementService(
            QueueTokenRepository queueTokenRepository,
            CounterRepository counterRepository,
            TokenScoringService tokenScoringService,
            RedisQueueService redisQueueService,
            WebSocketEventPublisher eventPublisher) {

        this.queueTokenRepository = queueTokenRepository;
        this.counterRepository = counterRepository;
        this.tokenScoringService = tokenScoringService;
        this.redisQueueService = redisQueueService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public QueueToken callNextToken(Long counterId) {

        // 1. Find the counter
        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid counterId"));

        // 2. Make sure counter has a service type
        if (counter.getCurrentServiceType() == null) {
            throw new IllegalStateException(
                    "Counter is not assigned to a service type");
        }

        ServiceType serviceType = counter.getCurrentServiceType();

        // 3. Lock all waiting tokens for this service type
        List<QueueToken> candidates =
                queueTokenRepository.findWaitingTokensForUpdate(serviceType);

        // 4. No waiting tokens
        if (candidates.isEmpty()) {
            throw new IllegalStateException(
                    "No waiting tokens for this service type");
        }

        // 5. Pick highest-scoring token
        QueueToken winner = candidates.stream()
                .max(Comparator.comparingDouble(
                        tokenScoringService::computeScore))
                .orElseThrow();

        // 6. Change token state
        winner.setStatus(TokenStatus.SERVING);
        winner.setCalledAt(LocalDateTime.now());
        winner.setCounter(counter);

        // 7. Save changes to MySQL
        QueueToken saved = queueTokenRepository.save(winner);

        // 8. Update Redis
        redisQueueService.markAsServing(
                saved.getId(),
                serviceType.getPrefix(),
                counterId
        );
        // Broadcasting token called event
        eventPublisher.publishTokenCalled(saved, counterId);

        return saved;
    }
}