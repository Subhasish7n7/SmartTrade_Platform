package smarttrade.backend.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.Mappers.UserMapper;
import smarttrade.backend.dto.auth.AuthResponse;
import smarttrade.backend.dto.auth.CurrentUserResponse;
import smarttrade.backend.dto.auth.LoginRequest;
import smarttrade.backend.dto.auth.RegisterRequest;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.security.CookieService;
import smarttrade.backend.service.auth.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticatedUserService authenticatedUserService;
    private final UserMapper userMapper;
    private final CookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        String token = authService.register(request);

        cookieService.setJwtCookie(response, token);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request,
                                      HttpServletResponse response) {

        String token = authService.login(request);
        cookieService.setJwtCookie(response, token);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        cookieService.clearJwtCookie(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser() {
        return ResponseEntity.ok(userMapper.mapToCurrentUser(authenticatedUserService.getCurrentUser()));
    }
}
