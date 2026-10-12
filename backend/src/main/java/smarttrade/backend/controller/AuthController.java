package smarttrade.backend.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.Mappers.UserMapper;
import smarttrade.backend.dto.auth.*;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.security.CookieService;
import smarttrade.backend.service.auth.AuthService;
import smarttrade.backend.service.auth.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import smarttrade.backend.security.SpaCsrfTokenRequestHandler;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final PasswordService passwordService;
    private final AuthenticatedUserService authenticatedUserService;
    private final UserMapper userMapper;
    private final CookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        String token = authService.register(request);
        cookieService.setJwtCookie(response, token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        String token = authService.login(request);
        cookieService.setJwtCookie(response, token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        cookieService.clearJwtCookie(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/csrf")
    public ResponseEntity<CsrfResponse> csrf(HttpServletRequest request) {
        Object value = request.getAttribute(SpaCsrfTokenRequestHandler.RAW_CSRF_TOKEN_ATTRIBUTE);

        if (!(value instanceof String token) || token.isBlank()) {
            throw new IllegalStateException("CSRF token was not initialized by Spring Security.");
        }

        return ResponseEntity.ok(new CsrfResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser() {
        return ResponseEntity.ok(userMapper.mapToCurrentUser(authenticatedUserService.getCurrentUser()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                               HttpServletResponse response) {
        String token = passwordService.changePassword(request);
        cookieService.setJwtCookie(response, token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return ResponseEntity.ok().build();
    }
}
