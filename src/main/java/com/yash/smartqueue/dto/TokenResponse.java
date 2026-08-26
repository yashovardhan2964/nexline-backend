package com.yash.smartqueue.dto;

import com.yash.smartqueue.model.QueueToken;

import java.time.LocalDateTime;

public class TokenResponse {
    private Long id;
    private String displayToken; // e.g. "B-14"
    private String status;
    private String priority;
    private LocalDateTime createdAt;
    private Double predictedWaitMinutes;

    public static TokenResponse fromEntity(QueueToken token) {
        TokenResponse response = new TokenResponse();
        response.id = token.getId();
        response.displayToken = token.getServiceType().getPrefix() + "-" + token.getTokenNumber();
        response.status = token.getStatus().name();
        response.priority = token.getPriority().name();
        response.createdAt = token.getCreatedAt();
        return response;
    }
    public static TokenResponse fromEntity(QueueToken token,
                                           Double predictedWait) {
        TokenResponse response = fromEntity(token);
        response.predictedWaitMinutes =
                (predictedWait != null && predictedWait > 0) ? predictedWait : null;
        return response;
    }

    public Long getId() { return id; }
    public String getDisplayToken() { return displayToken; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Double getPredictedWaitMinutes() { return predictedWaitMinutes; }
}