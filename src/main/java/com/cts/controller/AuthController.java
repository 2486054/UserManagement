package com.cts.controller;

import com.cts.dto.*;
import com.cts.service.AuthService;
import com.cts.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Authentication Controller", description = "Endpoints for user registration, login, password reset, and token management")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    public AuthController(AuthService authService, EmailService emailService) {
        this.authService = authService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Endpoint to register a new user with name, email, phone, role, and password")
    public ResponseEntity<String> register(@RequestBody RegisterRequest regDto) {
        String response = authService.registerUser(regDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user", description = "Endpoint to authenticate a user and return JWT access and refresh tokens")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginDto) {
        AuthResponse response = authService.loginUser(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Initiate password reset", description = "Endpoint to initiate the password reset process by sending a reset link to the user's email")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String resetLink = authService.forgotPassword(request);
        emailService.sendPasswordResetEmail(request.getEmail(), resetLink);
        return ResponseEntity.ok("Password reset link sent to your email!");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset user password", description = "Endpoint to reset the user's password using the token from the reset link")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        String response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT tokens", description = "Endpoint to refresh JWT access and refresh tokens using a valid refresh token")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Endpoint to invalidate the current refresh token and log the user out")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequest request) {
        String response = authService.logout(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all sessions", description = "Endpoint to invalidate all refresh tokens for the user and log out from all sessions")
    public ResponseEntity<String> logoutAll() {
        String email = org.springframework.security.core.context
                .SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        String response = authService.logoutAll(email);
        return ResponseEntity.ok(response);
    }
}