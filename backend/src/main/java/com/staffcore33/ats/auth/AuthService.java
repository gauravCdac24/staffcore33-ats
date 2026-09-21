package com.staffcore33.ats.auth;

import com.staffcore33.ats.audit.AuditService;
import com.staffcore33.ats.common.BadRequestException;
import com.staffcore33.ats.common.ResourceNotFoundException;
import com.staffcore33.ats.security.JwtService;
import com.staffcore33.ats.security.SecurityUtils;
import com.staffcore33.ats.user.User;
import com.staffcore33.ats.user.UserRepository;
import com.staffcore33.ats.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final AuditService auditService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        auditService.log("LOGIN", "USER", user.getId(), "User logged in");
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse me() {
        User user = SecurityUtils.currentUser();
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(request.getEmail())
                .orElse(null);
        // Always return success message to avoid email enumeration
        if (user != null) {
            String token = UUID.randomUUID().toString();
            resetTokenRepository.save(PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .used(false)
                    .createdAt(Instant.now())
                    .build());
            // In production this would email; for local/dev we return token
            return Map.of(
                    "message", "If the email exists, a reset token was created.",
                    "devToken", token
            );
        }
        return Map.of("message", "If the email exists, a reset token was created.");
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken reset = resetTokenRepository.findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
        if (reset.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Reset token expired");
        }
        User user = reset.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        reset.setUsed(true);
        auditService.log("PASSWORD_RESET", "USER", user.getId(), "Password reset completed");
    }

    public void logout() {
        try {
            auditService.log("LOGOUT", "USER", SecurityUtils.currentUserId(), "User logged out");
        } catch (Exception ignored) {
        }
    }
}
