package smarttrade.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.Mappers.UserMapper;
import smarttrade.backend.dto.user.*;
import smarttrade.backend.entities.user.ProfileVisibility;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.ChatService;
import smarttrade.backend.service.user.UserPreferenceService;
import smarttrade.backend.service.user.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticatedUserService authenticatedUserService;
    private final UserPreferenceService userPreferenceService;
    private final ChatService chatService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers().stream().map(userMapper::mapFrom).toList());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long userId) {
        return userService.GetUser(userId).map(user -> ResponseEntity.ok(userMapper.mapFrom(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/me/profile")
    public ResponseEntity<MyUserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userMapper.mapToMyProfile(authenticatedUserService.getCurrentUser()));
    }

    @PatchMapping("/users/me/profile")
    public ResponseEntity<MyUserProfileResponse> updateMyProfile(@Valid @RequestBody UpdateMyProfileRequest request) {
        return ResponseEntity.ok(userMapper.mapToMyProfile(userService.updateMyProfile(request)));
    }

    @GetMapping("/users/me/preferences")
    public ResponseEntity<UserPreferencesResponse> getMyPreferences() {
        return ResponseEntity.ok(userPreferenceService.getMyPreferences());
    }

    @PatchMapping("/users/me/preferences")
    public ResponseEntity<UserPreferencesResponse> updateMyPreferences(
            @Valid @RequestBody UpdateUserPreferencesRequest request) {
        UserPreferencesResponse before = userPreferenceService.getMyPreferences();
        UserPreferencesResponse updated = userPreferenceService.updateMyPreferences(request);
        if (request.getShowOnlineStatus() != null
                && before.isShowOnlineStatus() != updated.isShowOnlineStatus()) {
            chatService.refreshCurrentPresenceVisibility();
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/users/{userId}/profile")
    public ResponseEntity<PublicUserProfileResponse> getPublicProfile(@PathVariable Long userId) {
        UserEntity user = userService.GetUser(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (userPreferenceService.getForUser(user).getProfileVisibility() == ProfileVisibility.PRIVATE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userMapper.mapToPublicProfile(user));
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> addUser(@Valid @RequestBody UserDto user) {
        UserEntity saved = userService.addUser(userMapper.mapTo(user));
        userPreferenceService.getOrCreateForUser(saved);
        return new ResponseEntity<>(userMapper.mapFrom(saved), HttpStatus.CREATED);
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @Valid @RequestBody UserDto user) {
        if (userService.GetUser(userId).isEmpty()) return new ResponseEntity<>(user, HttpStatus.NOT_FOUND);
        UserDto dto = userMapper.mapFrom(userService.updateUser(userId, userMapper.mapTo(user)));
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
