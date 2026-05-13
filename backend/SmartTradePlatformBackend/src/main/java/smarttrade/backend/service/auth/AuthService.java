package smarttrade.backend.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import smarttrade.backend.dto.auth.AuthResponse;
import smarttrade.backend.dto.auth.LoginRequest;
import smarttrade.backend.dto.auth.RegisterRequest;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.repository.UserRepo;
import smarttrade.backend.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {

        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        /*
            Encode password before saving.

            NEVER store plain passwords.
        */
        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .name(request.getName())
                .phone_no(request.getPhone_no())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepo.save(user);

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {

        /*
            Spring Security automatically:
            - loads user
            - checks password
            - throws if invalid
        */
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(request.getEmail());

        return new AuthResponse(token);
    }
}
