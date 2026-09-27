package com.resourcebooking.service;

import com.resourcebooking.dto.LoginRequest;
import com.resourcebooking.dto.LoginResponse;
import com.resourcebooking.entity.User;
import com.resourcebooking.repository.UserRepository;
import com.resourcebooking.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        // this throws BadCredentialsException automatically if username/password don't match
        // which gets caught by GlobalExceptionHandler
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")); // shouldn't really happen at this point

        String token = jwtUtil.generateToken(user, user.getRole().name());

        return new LoginResponse(token, user.getUsername(), user.getRole().name());
    }
}
