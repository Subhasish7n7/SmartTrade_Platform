package smarttrade.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.Mappers.UserMapper;
import smarttrade.backend.dto.user.UserDto;
import smarttrade.backend.dto.user.MyUserProfileResponse;
import smarttrade.backend.dto.user.PublicUserProfileResponse;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.UserService;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticatedUserService authenticatedUserService;

    public UserController(
            UserService userService,
            UserMapper userMapper,
            AuthenticatedUserService authenticatedUserService
    ) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserEntity> users = userService.getAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(userMapper::mapFrom)
                .toList();
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long userId) {
        return userService.GetUser(userId)
                .map(userEntity -> ResponseEntity.ok(userMapper.mapFrom(userEntity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/me/profile")
    public ResponseEntity<MyUserProfileResponse> getMyProfile() {
        UserEntity currentUser = authenticatedUserService.getCurrentUser();
        return ResponseEntity.ok(userMapper.mapToMyProfile(currentUser));
    }

    @GetMapping("/users/{userId}/profile")
    public ResponseEntity<PublicUserProfileResponse> getPublicProfile(@PathVariable Long userId) {
        return userService.GetUser(userId)
                .map(user -> ResponseEntity.ok(userMapper.mapToPublicProfile(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> addUser(@Valid @RequestBody UserDto user) {
        UserEntity userEntity = userMapper.mapTo(user);
        UserDto userDto = userMapper.mapFrom(userService.addUser(userEntity));
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserDto user
    ) {
        if (userService.GetUser(userId).isEmpty()) {
            return new ResponseEntity<>(user, HttpStatus.NOT_FOUND);
        }

        UserEntity userEntity = userMapper.mapTo(user);
        UserDto userDto = userMapper.mapFrom(userService.updateUser(userId, userEntity));
        return ResponseEntity.ok(userDto);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
