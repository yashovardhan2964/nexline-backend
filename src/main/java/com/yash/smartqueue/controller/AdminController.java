package com.yash.smartqueue.controller;

import com.yash.smartqueue.model.Counter;
import com.yash.smartqueue.repository.CounterRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CounterRepository counterRepository;

    public AdminController(CounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    @GetMapping("/counters")
    public ResponseEntity<List<Counter>> getCounters() {
        return ResponseEntity.ok(counterRepository.findAll());
    }
}