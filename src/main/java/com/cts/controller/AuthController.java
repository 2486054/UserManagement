package com.cts.controller;

import com.cts.dto.*;
import com.cts.service.AuthService;
import com.cts.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    public AuthController(AuthService authService, EmailService emailService) {
        this.authService = authService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest regDto) {
        String response = authService.registerUser(regDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginDto) {
        AuthResponse response = authService.loginUser(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String resetLink = authService.forgotPassword(request);
        emailService.sendPasswordResetEmail(request.getEmail(), resetLink);
        return ResponseEntity.ok("Password reset link sent to your email!");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        String response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequest request) {
        String response = authService.logout(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout-all")
    public ResponseEntity<String> logoutAll() {
        String email = org.springframework.security.core.context
                .SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        String response = authService.logoutAll(email);
        return ResponseEntity.ok(response);
    }
}