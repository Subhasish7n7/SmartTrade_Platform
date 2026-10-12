package smarttrade.backend.service.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import smarttrade.backend.dto.auth.LoginRequest;
import smarttrade.backend.dto.auth.RegisterRequest;
import smarttrade.backend.entities.user.AccountStatus;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.entities.user.UserRole;
import smarttrade.backend.repository.user.UserRepo;
import smarttrade.backend.security.JwtService;
import smarttrade.backend.service.mail.EmailService;
import smarttrade.backend.service.user.UserPreferenceService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserPreferenceService userPreferenceService;
    private final EmailService emailService;

    @Transactional
    public String register(RegisterRequest request) {
        String email = request.getEmail().trim();
        if (userRepo.findByEmail(email).isPresent()) throw new RuntimeException("Email already exists");

        LocalDateTime now = LocalDateTime.now();
        UserEntity user = UserEntity.builder()
                .email(email)
                .name(request.getName())
                .phone_no(request.getPhone_no())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .accountStatus(AccountStatus.ACTIVE)
                .trustScore(500.0)
                .createdAt(now)
                .passwordChangedAt(now.truncatedTo(ChronoUnit.MILLIS))
                .build();
        UserEntity saved = userRepo.save(user);
        userPreferenceService.getOrCreateForUser(saved);
        return jwtService.generateToken(saved.getEmail());
    }

    public String login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getPassword()));

        userRepo.findByEmail(request.getEmail()).ifPresent(user -> {
            if (userPreferenceService.getForUser(user).isLoginAlerts()) {
                emailService.sendLoginAlertEmail(user.getEmail(), user.getName());
            }
        });
        return jwtService.generateToken(request.getEmail());
    }
}
