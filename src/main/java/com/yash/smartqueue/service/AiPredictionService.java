package com.yash.smartqueue.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiPredictionService {

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;
    private final RedisQueueService redisQueueService;

    public AiPredictionService(RedisQueueService redisQueueService) {
        this.restTemplate = new RestTemplate();
        this.redisQueueService = redisQueueService;
    }

    public double predictWaitTime(String serviceTypePrefix,
                                  double avgServiceMinutes,
                                  double priorityRatio) {
        try {
            // Build feature payload
            Map<String, Object> request = new HashMap<>();
            request.put("queueLength",
                    (int) redisQueueService.getQueueLength(serviceTypePrefix));
            request.put("activeCounters", 1); // will improve later with real counter count
            request.put("hourOfDay", LocalDateTime.now().getHour());
            request.put("dayOfWeek", LocalDateTime.now().getDayOfWeek().getValue() - 1);
            request.put("avgServiceMinutes", avgServiceMinutes);
            request.put("priorityRatio", priorityRatio);

            // Call Python AI service
            String url = aiServiceUrl + "/predict";
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null) {
                Object predicted = response.getBody().get("predictedWaitMinutes");
                return predicted instanceof Number
                        ? ((Number) predicted).doubleValue()
                        : -1.0;
            }

        } catch (ResourceAccessException e) {
            // AI service is down — fail gracefully, don't break token creation
            System.err.println("⚠️ AI service unavailable: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("⚠️ AI prediction failed: " + e.getMessage());
        }

        return -1.0; // -1 means prediction unavailable
    }
}