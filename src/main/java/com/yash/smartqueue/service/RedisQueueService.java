package com.yash.smartqueue.service;

import com.yash.smartqueue.model.QueueToken;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisQueueService {

    private final RedisTemplate<String, String> redisTemplate;

    // Key patterns
    private static final String QUEUE_KEY = "queue:";
    private static final String SERVING_KEY = "serving:";

    public RedisQueueService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Called when a new token is created — add to sorted set with its score
    public void addTokenToQueue(QueueToken token, double score) {
        String key = QUEUE_KEY + token.getServiceType().getPrefix();
        redisTemplate.opsForZSet().add(key, token.getId().toString(), score);
    }

    // Called when next token is called — remove from waiting, mark as serving
    public void markAsServing(Long tokenId, String serviceTypePrefix, Long counterId) {
        // Remove from waiting queue
        String queueKey = QUEUE_KEY + serviceTypePrefix;
        redisTemplate.opsForZSet().remove(queueKey, tokenId.toString());

        // Mark which token is serving at this counter
        String servingKey = SERVING_KEY + counterId;
        redisTemplate.opsForValue().set(servingKey, tokenId.toString());
    }

    // Called when token is completed or skipped
    public void removeFromServing(Long counterId) {
        String servingKey = SERVING_KEY + counterId;
        redisTemplate.delete(servingKey);
    }

    // How many tokens waiting for this service type
    public long getQueueLength(String serviceTypePrefix) {
        String key = QUEUE_KEY + serviceTypePrefix;
        Long size = redisTemplate.opsForZSet().size(key);
        return size != null ? size : 0;
    }

    // Get position of a specific token in the queue (1-based)
    public long getTokenPosition(Long tokenId, String serviceTypePrefix) {
        String key = QUEUE_KEY + serviceTypePrefix;
        // ZREVRANK gives position from highest score (0-based) — higher score = served sooner
        Long rank = redisTemplate.opsForZSet().reverseRank(key, tokenId.toString());
        return rank != null ? rank + 1 : -1; // +1 for 1-based position
    }

    // Which token is currently being served at a counter
    public String getCurrentlyServing(Long counterId) {
        return redisTemplate.opsForValue().get(SERVING_KEY + counterId);
    }

    // Update score for a waiting token (for aging recalculation)
    public void updateScore(Long tokenId, String serviceTypePrefix, double newScore) {
        String key = QUEUE_KEY + serviceTypePrefix;
        redisTemplate.opsForZSet().add(key, tokenId.toString(), newScore);
    }

    // Remove token from queue entirely (skip/expire)
    public void removeFromQueue(Long tokenId, String serviceTypePrefix) {
        String key = QUEUE_KEY + serviceTypePrefix;
        redisTemplate.opsForZSet().remove(key, tokenId.toString());
    }
}