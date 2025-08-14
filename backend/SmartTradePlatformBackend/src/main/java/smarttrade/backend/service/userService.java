package smarttrade.backend.service;

import smarttrade.backend.entities.userEntity;
import smarttrade.backend.repository.userRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class userService{
    userRepo userRepo;
    public List<userEntity> getAllusers() {
        return userRepo.findAll();
    }

    public Optional<userEntity> GetUser(Long user_Id) {
        return userRepo.findById(user_Id);
    }

    public userEntity addUser(userEntity user) {
        return userRepo.save(user);
    }

    public userEntity updateUser(Long userId, userEntity user) {
        user.setUser_id(userId);
        return userRepo.save(user);
    }

    public void deleteUser(Long userId) {
        userRepo.deleteById(userId);
    }
}
