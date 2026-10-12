package smarttrade.backend.service.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import smarttrade.backend.dto.auth.ChangePasswordRequest;
import smarttrade.backend.dto.auth.ResetPasswordRequest;
import smarttrade.backend.entities.auth.PasswordResetTokenEntity;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.auth.PasswordResetTokenRepo;
import smarttrade.backend.repository.user.UserRepo;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.security.JwtService;
import smarttrade.backend.service.mail.EmailService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;
    private static final int RESET_TOKEN_MINUTES = 30;

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepo tokenRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Transactional
    public String changePassword(ChangePasswordRequest request) {
        UserEntity user = authenticatedUserService.getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        userRepo.save(user);
        tokenRepo.deleteUnusedForUser(user.getUserId());
        return jwtService.generateToken(user.getEmail());
    }

    @Transactional
    public void requestPasswordReset(String email) {
        // Keep the public endpoint generic even when SMTP is not configured.
        if (!emailService.isEnabled()) {
            log.warn("Password reset requested while outbound email is disabled");
            return;
        }
        UserEntity user = userRepo.findByEmail(email.trim())
                .orElse(null);
        if (user == null) return;

        tokenRepo.deleteUnusedForUser(user.getUserId());
        String rawToken = generateRawToken();
        PasswordResetTokenEntity token = tokenRepo.save(PasswordResetTokenEntity.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .expiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTES))
                .createdAt(LocalDateTime.now())
                .build());

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), rawToken);
        } catch (RuntimeException ex) {
            tokenRepo.delete(token);
            log.warn("Could not send password reset email; the public response remains generic");
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetTokenEntity token = tokenRepo.findUnusedByTokenHashForUpdate(hashToken(request.getToken().trim()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));
        LocalDateTime now = LocalDateTime.now();
        if (!token.getExpiresAt().isAfter(now)) {
            throw new IllegalArgumentException("Invalid or expired password reset token");
        }

        UserEntity user = userRepo.findById(token.getUser().getUserId())
                .orElseThrow(() -> new IllegalStateException("Reset account no longer exists"));
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(now.truncatedTo(ChronoUnit.MILLIS));
        userRepo.save(user);
        token.setUsedAt(now);
        tokenRepo.save(token);
        tokenRepo.deleteUnusedForUser(user.getUserId());
    }

    private String generateRawToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash reset token", ex);
        }
    }
}
