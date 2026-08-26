package com.yash.smartqueue.controller;

import com.yash.smartqueue.model.Role;
import com.yash.smartqueue.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.phone, request.password);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request) {
        String message = authService.register(
                request.name, request.phone, request.password, request.role);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // Inner DTO classes — small enough to keep here
    static class LoginRequest {
        @NotBlank public String phone;
        @NotBlank public String password;
    }

    static class RegisterRequest {
        @NotBlank public String name;
        @NotBlank public String phone;
        @NotBlank public String password;
        @NotNull public Role role;
    }
}