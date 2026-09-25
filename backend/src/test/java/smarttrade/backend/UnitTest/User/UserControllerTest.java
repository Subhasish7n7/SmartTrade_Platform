package smarttrade.backend.UnitTest.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import smarttrade.backend.Mappers.UserMapper;
import smarttrade.backend.TestDataUtil.userTestData;
import smarttrade.backend.controller.UserController;
import smarttrade.backend.dto.user.MyUserProfileResponse;
import smarttrade.backend.dto.user.PublicUserProfileResponse;
import smarttrade.backend.dto.user.UserDto;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    private UserEntity user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .build();

        user = userTestData.CreateUserA();
        user.setUserId(1L);

        userDto = new UserDto(
                1L,
                user.getEmail(),
                user.getName(),
                null,
                user.getPhone_no(),
                user.getTrustScore(),
                user.getTotalListings(),
                user.getSuccessfulTrades(),
                user.getLatitude(),
                user.getLongitude()
        );
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(user));
        when(userMapper.mapFrom(user)).thenReturn(userDto);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].email").value(user.getEmail()))
                .andExpect(jsonPath("$[0].name").value(user.getName()));

        verify(userService).getAllUsers();
        verify(userMapper).mapFrom(user);
    }

    @Test
    void shouldGetUser() throws Exception {
        when(userService.GetUser(1L)).thenReturn(Optional.of(user));
        when(userMapper.mapFrom(user)).thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value(user.getEmail()));

        verify(userService).GetUser(1L);
        verify(userMapper).mapFrom(user);
    }

    @Test
    void shouldReturnNotFoundForUnknownUser() throws Exception {
        when(userService.GetUser(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());

        verify(userMapper, never()).mapFrom(any(UserEntity.class));
    }

    @Test
    void shouldGetMyProfile() throws Exception {
        MyUserProfileResponse response = new MyUserProfileResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhone_no(),
                user.getAvatar().getSecureUrl(),
                user.getTrustScore(),
                user.getTotalListings(),
                user.getSuccessfulTrades()
        );

        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(userMapper.mapToMyProfile(user)).thenReturn(response);

        mockMvc.perform(get("/users/me/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.phoneNo").value(user.getPhone_no()))
                .andExpect(jsonPath("$.avatarUrl").value(user.getAvatar().getSecureUrl()))
                .andExpect(jsonPath("$.trustScore").value(user.getTrustScore()))
                .andExpect(jsonPath("$.totalListings").value(user.getTotalListings()))
                .andExpect(jsonPath("$.successfulTrades").value(user.getSuccessfulTrades()));
    }

    @Test
    void shouldGetPublicProfile() throws Exception {
        PublicUserProfileResponse response =
                new PublicUserProfileResponse(
                        user.getUserId(),
                        user.getName(),
                        user.getAvatar().getSecureUrl(),
                        user.getTrustScore(),
                        user.getTotalListings(),
                        user.getSuccessfulTrades()
                );

        when(userService.GetUser(1L)).thenReturn(Optional.of(user));
        when(userMapper.mapToPublicProfile(user)).thenReturn(response);

        mockMvc.perform(get("/users/1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.avatarUrl").value(user.getAvatar().getSecureUrl()))
                .andExpect(jsonPath("$.trustScore").value(user.getTrustScore()))
                .andExpect(jsonPath("$.totalListings").value(user.getTotalListings()))
                .andExpect(jsonPath("$.successfulTrades").value(user.getSuccessfulTrades()));
    }

    @Test
    void shouldReturnNotFoundForUnknownPublicProfile() throws Exception {
        when(userService.GetUser(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/999/profile"))
                .andExpect(status().isNotFound());

        verify(userMapper, never()).mapToPublicProfile(any(UserEntity.class));
    }

    @Test
    void shouldAddUser() throws Exception {
        UserDto request = new UserDto(
                null,
                "new@test.com",
                "New User",
                "password",
                "9999999999",
                0.0,
                0,
                0,
                20.0,
                85.0
        );

        UserEntity mappedEntity = UserEntity.builder()
                .email("new@test.com")
                .name("New User")
                .phone_no("9999999999")
                .build();

        UserEntity savedEntity = UserEntity.builder()
                .userId(2L)
                .email("new@test.com")
                .name("New User")
                .phone_no("9999999999")
                .build();

        UserDto response = new UserDto(
                2L,
                "new@test.com",
                "New User",
                null,
                "9999999999",
                0.0,
                0,
                0,
                null,
                null
        );

        when(userMapper.mapTo(any(UserDto.class))).thenReturn(mappedEntity);
        when(userService.addUser(mappedEntity)).thenReturn(savedEntity);
        when(userMapper.mapFrom(savedEntity)).thenReturn(response);

        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "new@test.com",
                                          "name": "New User",
                                          "password": "password",
                                          "phone_no": "9999999999"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.name").value("New User"));

        verify(userService).addUser(mappedEntity);
    }

    @Test
    void shouldRejectAddUserWithInvalidEmail() throws Exception {
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "not-an-email",
                                          "name": "New User"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService, userMapper);
    }

    @Test
    void shouldRejectAddUserWithoutEmail() throws Exception {
        mockMvc.perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "New User"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService, userMapper);
    }

    @Test
    void shouldUpdateExistingUser() throws Exception {
        when(userService.GetUser(1L)).thenReturn(Optional.of(user));
        when(userMapper.mapTo(any(UserDto.class))).thenReturn(user);
        when(userService.updateUser(eq(1L), eq(user))).thenReturn(user);
        when(userMapper.mapFrom(user)).thenReturn(userDto);

        mockMvc.perform(
                        patch("/users/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "user@test.com",
                                          "name": "Updated User",
                                          "phone_no": "9876543210"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value(user.getEmail()));

        verify(userService).updateUser(eq(1L), eq(user));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownUser() throws Exception {
        when(userService.GetUser(999L)).thenReturn(Optional.empty());

        mockMvc.perform(
                        patch("/users/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "user@test.com",
                                          "name": "Updated User"
                                        }
                                        """)
                )
                .andExpect(status().isNotFound());

        verify(userService, never()).updateUser(anyLong(), any(UserEntity.class));
        verify(userMapper, never()).mapTo(any(UserDto.class));
    }

    @Test
    void shouldDeleteUser() throws Exception {
        userController.deleteUser(1L);

        verify(userService).deleteUser(1L);
    }
}