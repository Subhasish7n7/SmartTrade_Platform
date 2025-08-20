package smarttrade.backend.controller;

import smarttrade.backend.Mappers.userMapperImpl;
import smarttrade.backend.dto.userDto;
import smarttrade.backend.entities.userEntity;
import smarttrade.backend.service.userService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class userController {

        private userService userService;
        private userMapperImpl userMapper;
        public userController(userService userService, userMapperImpl userMapper){
            this.userService=userService;
            this.userMapper=userMapper;

        }
        @GetMapping("/users")
        public ResponseEntity<List<userDto>> getAllUsers(){
            List<userEntity> users= userService.getAllusers();
            List<userDto> userDtos= users.stream().map(userMapper::mapFrom).toList();
            return new ResponseEntity<>(userDtos, HttpStatus.OK);
        }
        @GetMapping("users/{user_id}")
        public ResponseEntity<userDto> getUsers(@PathVariable("user_id") Long user_id){
            userEntity user= userService.GetUser(user_id).orElseThrow(
                    ()-> new IllegalArgumentException("object not found"));

            return new ResponseEntity<>(userMapper.mapFrom(user), HttpStatus.OK);
        }
        @PostMapping("/users")
        public ResponseEntity<userDto> addUser(userDto user){
            userEntity userEntity= userMapper.mapTo(user);
            userDto userDto=userMapper.mapFrom(userService.addUser(userEntity));
            return new ResponseEntity<>(userDto, HttpStatus.CREATED);
        }
        @PatchMapping("/users/{user_id}")
        public ResponseEntity<userDto> UpdateItem(@PathVariable("user_id") Long user_id,
                                                    @RequestBody userDto user){
            if(userService.GetUser(user_id).isEmpty()){
                return new ResponseEntity<>(user,HttpStatus.NOT_FOUND);
            }
            userEntity userEntity=userMapper.mapTo(user);
            userDto userDto=userMapper.mapFrom(userService.updateUser(user_id,userEntity));
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        }
        @DeleteMapping("/users/{user_id}")
        public ResponseEntity deleteItem(@PathVariable("user_id") Long user_id){
            userService.deleteUser(user_id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

}
