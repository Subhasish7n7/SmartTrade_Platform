package smarttrade.backend.UnitTest.Auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import smarttrade.backend.Mappers.UserMapper;
import smarttrade.backend.TestDataUtil.userTestData;
import smarttrade.backend.controller.AuthController;
import smarttrade.backend.dto.auth.CurrentUserResponse;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.security.CookieService;
import smarttrade.backend.service.auth.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import smarttrade.backend.security.SpaCsrfTokenRequestHandler;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CookieService cookieService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .build();

        user = userTestData.CreateUserA();
        user.setUserId(1L);
    }

    @Test
    void shouldRegisterAndSetJwtCookie() throws Exception {
        when(authService.register(any())).thenReturn("jwt-token");

        mockMvc.perform(
                        post("/auth/register")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "email": "user@test.com",
                                          "name": "Test User",
                                          "password": "password",
                                          "phone_no": "9876543210"
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        verify(authService).register(any());
        verify(cookieService).setJwtCookie(any(), eq("jwt-token"));
    }

    @Test
    void shouldLoginAndSetJwtCookie() throws Exception {
        when(authService.login(any())).thenReturn("jwt-token");

        mockMvc.perform(
                        post("/auth/login")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "email": "user@test.com",
                                          "password": "password"
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        verify(authService).login(any());
        verify(cookieService).setJwtCookie(any(), eq("jwt-token"));
    }

    @Test
    void shouldRejectLoginWithInvalidEmail() throws Exception {
        mockMvc.perform(
                        post("/auth/login")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "email": "not-an-email",
                                          "password": "password"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService, cookieService);
    }

    @Test
    void shouldRejectLoginWithoutPassword() throws Exception {
        mockMvc.perform(
                        post("/auth/login")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "email": "user@test.com"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService, cookieService);
    }

    @Test
    void shouldLogoutByClearingJwtCookie() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk());

        verify(cookieService).clearJwtCookie(any());
    }

    @Test
    void shouldReturnCsrfToken() throws Exception {
        mockMvc.perform(
                        get("/auth/csrf")
                                .requestAttr(
                                        SpaCsrfTokenRequestHandler.RAW_CSRF_TOKEN_ATTRIBUTE,
                                        "csrf-token"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("csrf-token"));
    }

    @Test
    void shouldReturnCurrentUser() throws Exception {
        CurrentUserResponse response = CurrentUserResponse.builder()
                .id(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNo(user.getPhone_no())
                .avatarUrl(user.getAvatar().getSecureUrl())
                .build();

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(userMapper.mapToCurrentUser(user)).thenReturn(response);

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getUserId()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.phoneNo").value(user.getPhone_no()))
                .andExpect(jsonPath("$.avatarUrl").value(user.getAvatar().getSecureUrl()));

        verify(authenticatedUserService).getCurrentUser();
        verify(userMapper).mapToCurrentUser(user);
    }
}