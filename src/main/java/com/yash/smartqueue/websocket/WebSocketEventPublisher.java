package com.yash.smartqueue.websocket;

import com.yash.smartqueue.model.QueueToken;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class WebSocketEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishTokenCreated(QueueToken token) {
        String servicePrefix = token.getServiceType().getPrefix();

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "TOKEN_CREATED");
        payload.put("tokenId", token.getId());
        payload.put("displayToken", servicePrefix + "-" + token.getTokenNumber());
        payload.put("priority", token.getPriority().name());

        messagingTemplate.convertAndSend(
                "/topic/queue/" + servicePrefix, (Object) payload);
    }

    public void publishTokenCalled(QueueToken token, Long counterId) {
        String servicePrefix = token.getServiceType().getPrefix();

        Map<String, Object> queuePayload = new HashMap<>();
        queuePayload.put("event", "TOKEN_CALLED");
        queuePayload.put("tokenId", token.getId());
        queuePayload.put("displayToken", servicePrefix + "-" + token.getTokenNumber());
        queuePayload.put("counterId", counterId);
        queuePayload.put("customerPhone", token.getCustomerPhone());

        messagingTemplate.convertAndSend(
                "/topic/queue/" + servicePrefix, (Object) queuePayload);

        Map<String, Object> counterPayload = new HashMap<>();
        counterPayload.put("event", "NOW_SERVING");
        counterPayload.put("displayToken", servicePrefix + "-" + token.getTokenNumber());

        messagingTemplate.convertAndSend(
                "/topic/counter/" + counterId, (Object) counterPayload);
    }

    public void publishTokenCompleted(QueueToken token) {
        String servicePrefix = token.getServiceType().getPrefix();

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "TOKEN_COMPLETED");
        payload.put("tokenId", token.getId());
        payload.put("displayToken", servicePrefix + "-" + token.getTokenNumber());

        messagingTemplate.convertAndSend(
                "/topic/queue/" + servicePrefix, (Object) payload);
    }

    public void publishTokenSkipped(QueueToken token) {
        String servicePrefix = token.getServiceType().getPrefix();

        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "TOKEN_SKIPPED");
        payload.put("tokenId", token.getId());
        payload.put("displayToken", servicePrefix + "-" + token.getTokenNumber());

        messagingTemplate.convertAndSend(
                "/topic/queue/" + servicePrefix, (Object) payload);
    }
}