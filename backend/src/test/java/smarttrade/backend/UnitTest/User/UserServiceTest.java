package smarttrade.backend.UnitTest.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import smarttrade.backend.TestDataUtil.userTestData;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.UserRepo;
import smarttrade.backend.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserService userService;

    private UserEntity user;
    private UserEntity otherUser;

    @BeforeEach
    void setUp() {
        user = userTestData.CreateUserA();
        user.setUserId(1L);

        otherUser = userTestData.CreateUserB();
        otherUser.setUserId(2L);
    }

    @Test
    void shouldGetAllUsers() {
        when(userRepo.findAll()).thenReturn(List.of(user, otherUser));

        List<UserEntity> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals(user, result.get(0));
        assertEquals(otherUser, result.get(1));
        verify(userRepo).findAll();
    }

    @Test
    void shouldGetUserByIdWhenPresent() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        Optional<UserEntity> result = userService.GetUser(1L);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepo).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        when(userRepo.findById(999L)).thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.GetUser(999L);

        assertTrue(result.isEmpty());
        verify(userRepo).findById(999L);
    }

    @Test
    void shouldAddUser() {
        when(userRepo.save(user)).thenReturn(user);

        UserEntity result = userService.addUser(user);

        assertSame(user, result);
        verify(userRepo).save(user);
    }

    @Test
    void shouldUpdateUserAndForceRequestedId() {
        UserEntity update = userTestData.CreateUserC();
        update.setUserId(999L);
        update.setEmail("updated@test.com");
        update.setName("Updated");
        update.setPhone_no("1234567890");
        update.setLatitude(21.0);
        update.setLongitude(86.0);

        when(userRepo.save(update)).thenReturn(update);

        UserEntity result = userService.updateUser(1L, update);

        assertEquals(1L, update.getUserId());
        assertSame(update, result);
        verify(userRepo).save(update);
    }

    @Test
    void shouldDeleteUserById() {
        userService.deleteUser(1L);

        verify(userRepo).deleteById(1L);
    }
}