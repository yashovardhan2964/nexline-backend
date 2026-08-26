package com.yash.smartqueue.controller;

import com.yash.smartqueue.dto.CreateTokenRequest;
import com.yash.smartqueue.dto.TokenResponse;
import com.yash.smartqueue.model.QueueToken;
import com.yash.smartqueue.model.ServiceType;
import com.yash.smartqueue.repository.ServiceTypeRepository;
import com.yash.smartqueue.service.QueueTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tokens")
public class QueueTokenController {

    private final QueueTokenService queueTokenService;
    private final ServiceTypeRepository serviceTypeRepository;

    public QueueTokenController(QueueTokenService queueTokenService,
                                ServiceTypeRepository serviceTypeRepository) {
        this.queueTokenService = queueTokenService;
        this.serviceTypeRepository = serviceTypeRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createToken(
            @Valid @RequestBody CreateTokenRequest request) {

        ServiceType serviceType = serviceTypeRepository
                .findById(request.getServiceTypeId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid serviceTypeId"));

        Map<String, Object> result = queueTokenService
                .createTokenWithPrediction(
                        serviceType,
                        request.getCustomerName(),
                        request.getCustomerPhone()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
