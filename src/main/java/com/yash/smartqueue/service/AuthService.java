package com.yash.smartqueue.service;

import com.yash.smartqueue.model.Role;
import com.yash.smartqueue.model.User;
import com.yash.smartqueue.repository.UserRepository;
import com.yash.smartqueue.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(String phone, String password) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Invalid phone or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid phone or password");
        }

        return jwtUtil.generateToken(user.getPhone(), user.getRole().name());
    }

    public String register(String name, String phone, String password, Role role) {
        if (userRepository.findByPhone(phone).isPresent()) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password)); // hash before saving
        user.setRole(role);

        userRepository.save(user);
        return "User registered successfully";
    }
}