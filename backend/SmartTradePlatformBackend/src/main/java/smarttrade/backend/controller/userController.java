package smarttrade.backend.controller;

import jakarta.validation.Valid;
import smarttrade.backend.Mappers.userMapperImpl;
import smarttrade.backend.dto.userDto;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.service.userService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class userController {

    private final userService userService;
    private final userMapperImpl userMapper;

    public userController(userService userService, userMapperImpl userMapper){
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    public ResponseEntity<List<userDto>> getAllUsers(){
        List<UserEntity> users = userService.getAllUsers();
        List<userDto> userDtos = users.stream().map(userMapper::mapFrom).toList();
        return new ResponseEntity<>(userDtos, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<userDto> getUser(@PathVariable("userId") Long userId) {
        return userService.GetUser(userId).map(userEntity -> {
            userDto userDto= userMapper.mapFrom(userEntity);
            return new ResponseEntity<>(userDto,HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @PostMapping("/users")
    public ResponseEntity<userDto> addUser(@Valid @RequestBody userDto user){
        UserEntity userEntity = userMapper.mapTo(user);
        userDto userDto = userMapper.mapFrom(userService.addUser(userEntity));
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PatchMapping("/users/{userId}")
    public ResponseEntity<userDto> updateUser(@PathVariable("userId") Long userId,
                                              @Valid @RequestBody userDto user){
        if(userService.GetUser(userId).isEmpty()){
            return new ResponseEntity<>(user, HttpStatus.NOT_FOUND);
        }
        UserEntity userEntity = userMapper.mapTo(user);
        userDto userDto = userMapper.mapFrom(userService.updateUser(userId, userEntity));
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") Long userId){
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

