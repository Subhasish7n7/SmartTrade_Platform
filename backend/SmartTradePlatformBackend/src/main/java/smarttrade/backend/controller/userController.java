package smarttrade.backend.controller;

import smarttrade.backend.entities.userEntity;
import smarttrade.backend.service.userService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class userController {

        private userService userService;
        public userController(userService userService){
            this.userService=userService;

        }
        @GetMapping("/users")
        public ResponseEntity<List<userEntity>> getAllUsers(){
            List<userEntity> users= userService.getAllusers();
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
        @GetMapping("users/{user_id}")
        public ResponseEntity<userEntity> getUsers(@PathVariable("user_id") Long user_id){
            userEntity user= userService.GetUser(user_id).orElseThrow(
                    ()-> new IllegalArgumentException("object not found"));

            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        @PostMapping("/users")
        public ResponseEntity<userEntity> addUser(userEntity user){
            return new ResponseEntity<>(userService.addUser(user), HttpStatus.CREATED);
        }
        @PatchMapping("/users/{user_id}")
        public ResponseEntity<userEntity> UpdateItem(@PathVariable("user_id") Long user_id,
                                                     userEntity user){
            if(userService.GetUser(user_id).isEmpty()){
                return new ResponseEntity<>(user,HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(userService.updateUser(user_id,user), HttpStatus.OK);
        }
        @DeleteMapping("/users/{user_id}")
        public ResponseEntity deleteItem(@PathVariable("user_id") Long user_id){
            userService.deleteUser(user_id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

}
