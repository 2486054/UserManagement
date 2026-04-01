package com.cts.service;

import com.cts.dto.*;
import com.cts.entity.PasswordResetToken;
import com.cts.entity.RefreshToken;
import com.cts.entity.User;
import com.cts.exception.InvalidCredentialsException;
import com.cts.exception.UserAlreadyExistsException;
import com.cts.exception.UserNotFoundException;
import com.cts.repository.PasswordResetTokenRepository;
import com.cts.repository.RefreshTokenRepository;
import com.cts.repository.UserRepository;
import com.cts.security.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.reset-password-url}")
    private String resetPasswordUrl;

    public AuthService(UserRepository userRepository,
                       BCryptPasswordEncoder encoder,
                       JwtUtils jwtUtils,
                       PasswordResetTokenRepository tokenRepository,
                       EmailService emailService,
                       RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtils = jwtUtils;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String registerUser(RegisterRequest regDto) {
        if (userRepository.findByEmail(regDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already registered!");
        }
        User user = new User();
        user.setName(regDto.getName());
        user.setEmail(regDto.getEmail().trim());
        user.setPhone(regDto.getPhone());
        user.setRole(regDto.getRole());
        user.setPassword(encoder.encode(regDto.getPassword()));
        userRepository.save(user);
        return "User registered successfully!";
    }

    @Transactional
    public AuthResponse loginUser(LoginRequest loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail().trim())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (encoder.matches(loginDto.getPassword(), user.getPassword())) {

            String accessToken = jwtUtils.generateToken(
                    user.getEmail(), user.getRole().name());

            String refreshTokenStr = jwtUtils.generateRefreshToken(user.getEmail());

            refreshTokenRepository.deleteByUserId(user.getUserId());

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken(refreshTokenStr);
            refreshToken.setUserId(user.getUserId());
            refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
            refreshToken.setRevoked(false);
            refreshTokenRepository.save(refreshToken);

            return new AuthResponse(accessToken, refreshTokenStr,
                    user.getRole(), "Login successful!");
        }

        throw new InvalidCredentialsException("Invalid Credentials");
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        String token = request.getRefreshToken();

        RefreshToken savedToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token. Please login again."));

        if (savedToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked. Please login again.");
        }

        if (savedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(savedToken);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }

        User user = userRepository.findById(savedToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String newAccessToken = jwtUtils.generateToken(
                user.getEmail(), user.getRole().name());

        return new AuthResponse(newAccessToken, token,
                user.getRole(), "Token refreshed successfully!");
    }

    @Transactional
    public String logout(RefreshTokenRequest request) {

        RefreshToken savedToken = refreshTokenRepository.findByToken(
                        request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        savedToken.setRevoked(true);
        refreshTokenRepository.save(savedToken);

        return "Logged out successfully!";
    }

    @Transactional
    public String logoutAll(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        refreshTokenRepository.deleteByUserId(user.getUserId());

        return "Logged out from all devices successfully!";
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new UserNotFoundException("No account found with this email"));

        tokenRepository.deleteByUserUserId(user.getUserId());

        String resetToken = UUID.randomUUID().toString();
        String resetLink = resetPasswordUrl + "?token=" + resetToken;

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(resetToken);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        passwordResetToken.setUsed(false);
        tokenRepository.save(passwordResetToken);

        return resetLink;
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Reset token has already been used");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired. Please request a new one");
        }
        User user = resetToken.getUser();
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        return "Password reset successfully!";
    }
}