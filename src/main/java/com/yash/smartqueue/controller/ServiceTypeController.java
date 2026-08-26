package com.yash.smartqueue.controller;

import com.yash.smartqueue.model.ServiceType;
import com.yash.smartqueue.repository.ServiceTypeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-types")
public class ServiceTypeController {

    private final ServiceTypeRepository serviceTypeRepository;

    public ServiceTypeController(ServiceTypeRepository serviceTypeRepository) {
        this.serviceTypeRepository = serviceTypeRepository;
    }

    @GetMapping
    public ResponseEntity<List<ServiceType>> getAllServiceTypes() {
        return ResponseEntity.ok(serviceTypeRepository.findAll());
    }
}