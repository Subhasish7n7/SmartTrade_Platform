package smarttrade.backend.controller;

import jakarta.validation.Valid;
import smarttrade.backend.Mappers.UserMapperImpl;
import smarttrade.backend.dto.UserDto;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class UserController {

    private final UserService userService;
    private final UserMapperImpl userMapper;

    public UserController(UserService userService, UserMapperImpl userMapper){
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers(){
        List<UserEntity> users = userService.getAllUsers();
        List<UserDto> UserDtos = users.stream().map(userMapper::mapFrom).toList();
        return new ResponseEntity<>(UserDtos, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable("userId") Long userId) {
        return userService.GetUser(userId).map(userEntity -> {
            UserDto userDto= userMapper.mapFrom(userEntity);
            return new ResponseEntity<>(userDto,HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @PostMapping("/users")
    public ResponseEntity<UserDto> addUser(@Valid @RequestBody UserDto user){
        UserEntity userEntity = userMapper.mapTo(user);
        UserDto userDto = userMapper.mapFrom(userService.addUser(userEntity));
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable("userId") Long userId,
                                              @Valid @RequestBody UserDto user){
        if(userService.GetUser(userId).isEmpty()){
            return new ResponseEntity<>(user, HttpStatus.NOT_FOUND);
        }
        UserEntity userEntity = userMapper.mapTo(user);
        UserDto userDto = userMapper.mapFrom(userService.updateUser(userId, userEntity));
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") Long userId){
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

