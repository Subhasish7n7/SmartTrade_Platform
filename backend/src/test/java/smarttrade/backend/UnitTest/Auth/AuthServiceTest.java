package smarttrade.backend.UnitTest.Auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import smarttrade.backend.dto.auth.LoginRequest;
import smarttrade.backend.dto.auth.RegisterRequest;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.user.UserRepo;
import smarttrade.backend.security.JwtService;
import smarttrade.backend.service.auth.AuthService;
import smarttrade.backend.service.mail.EmailService;
import smarttrade.backend.service.user.UserPreferenceService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;
    
    @Mock
    private UserPreferenceService userPreferenceService;

    @Mock
    private EmailService emailService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("user@test.com");
        registerRequest.setName("Test User");
        registerRequest.setPassword("plain-password");
        registerRequest.setPhone_no("9876543210");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("user@test.com");
        loginRequest.setPassword("plain-password");
    }

    @Test
    void shouldRegisterUserWithEncodedPasswordAndReturnJwt() {
        when(userRepo.findByEmail("user@test.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password"))
                .thenReturn("encoded-password");
        when(userRepo.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken("user@test.com"))
                .thenReturn("jwt-token");

        String result = authService.register(registerRequest);

        assertEquals("jwt-token", result);

        ArgumentCaptor<UserEntity> captor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(userRepo).save(captor.capture());

        UserEntity saved = captor.getValue();

        assertEquals("user@test.com", saved.getEmail());
        assertEquals("Test User", saved.getName());
        assertEquals("9876543210", saved.getPhone_no());
        assertEquals("encoded-password", saved.getPassword());

        verify(passwordEncoder).encode("plain-password");
        verify(jwtService).generateToken("user@test.com");
    }

    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() {
        UserEntity existing = UserEntity.builder()
                .userId(1L)
                .email("user@test.com")
                .build();

        when(userRepo.findByEmail("user@test.com"))
                .thenReturn(Optional.of(existing));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(registerRequest)
        );

        assertEquals("Email already exists", exception.getMessage());

        verify(userRepo, never()).save(any(UserEntity.class));
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void shouldLoginThroughAuthenticationManagerAndReturnJwt() {
        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenReturn(null);

        when(jwtService.generateToken("user@test.com"))
                .thenReturn("jwt-token");

        String result = authService.login(loginRequest);

        assertEquals("jwt-token", result);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);

        verify(authenticationManager).authenticate(captor.capture());

        UsernamePasswordAuthenticationToken authentication =
                captor.getValue();

        assertEquals("user@test.com", authentication.getPrincipal());
        assertEquals("plain-password", authentication.getCredentials());

        verify(jwtService).generateToken("user@test.com");
    }

    @Test
    void shouldNotGenerateJwtWhenAuthenticationFails() {
        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class
        ))).thenThrow(new RuntimeException("Authentication failed"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Authentication failed", exception.getMessage());
        verify(jwtService, never()).generateToken(anyString());
    }
}
