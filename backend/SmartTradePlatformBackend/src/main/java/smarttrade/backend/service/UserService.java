package smarttrade.backend.service;

import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.repository.UserRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class UserService {
    UserRepo userRepo;

    public UserService(UserRepo userRepo) {
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
