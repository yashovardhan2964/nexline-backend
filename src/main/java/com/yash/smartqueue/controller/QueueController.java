package com.yash.smartqueue.controller;

import com.yash.smartqueue.dto.TokenResponse;
import com.yash.smartqueue.model.QueueToken;
import com.yash.smartqueue.service.QueueAdvancementService;
import com.yash.smartqueue.service.QueueTokenService;
import com.yash.smartqueue.service.RedisQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueAdvancementService queueAdvancementService;
    private final RedisQueueService redisQueueService;
    private final QueueTokenService queueTokenService;

    public QueueController(QueueAdvancementService queueAdvancementService,
                           RedisQueueService redisQueueService,
                           QueueTokenService queueTokenService) {
        this.queueAdvancementService = queueAdvancementService;
        this.redisQueueService = redisQueueService;
        this.queueTokenService = queueTokenService;
    }

    @PostMapping("/next/{counterId}")
    public ResponseEntity<TokenResponse> callNext(@PathVariable Long counterId) {
        QueueToken token = queueAdvancementService.callNextToken(counterId);
        return ResponseEntity.ok(TokenResponse.fromEntity(token));
    }

    @GetMapping("/status/{serviceTypePrefix}/{tokenId}")
    public ResponseEntity<Map<String, Object>> getQueueStatus(
            @PathVariable String serviceTypePrefix,
            @PathVariable Long tokenId) {

        long position = redisQueueService.getTokenPosition(tokenId, serviceTypePrefix);
        long queueLength = redisQueueService.getQueueLength(serviceTypePrefix);

        Map<String, Object> status = new HashMap<>();
        status.put("tokenId", tokenId);
        status.put("position", position == -1 ? "Not in queue" : position);
        status.put("totalWaiting", queueLength);

        return ResponseEntity.ok(status);
    }

    @PutMapping("/tokens/{id}/complete")
    public ResponseEntity<TokenResponse> completeToken(@PathVariable Long id) {
        QueueToken token = queueTokenService.completeToken(id);
        return ResponseEntity.ok(TokenResponse.fromEntity(token));
    }

    @PutMapping("/tokens/{id}/skip")
    public ResponseEntity<TokenResponse> skipToken(@PathVariable Long id) {
        QueueToken token = queueTokenService.skipToken(id);
        return ResponseEntity.ok(TokenResponse.fromEntity(token));
    }
}