package smarttrade.backend.service;

import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.repository.userRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class userService{
    userRepo userRepo;

    public userService(userRepo userRepo) {
        this.userRepo = userRepo;
    }

    public List<UserEntity> getAllUsers() {
        return userRepo.findAll();
    }

    public Optional<UserEntity> GetUser(Long user_Id) {
        return userRepo.findById(user_Id);
    }

    public UserEntity addUser(UserEntity user) {
        return userRepo.save(user);
    }

    public UserEntity updateUser(Long userId, UserEntity user) {
        user.setUserId(userId);
        return userRepo.save(user);
    }

    public void deleteUser(Long userId) {
        userRepo.deleteById(userId);
    }
}
