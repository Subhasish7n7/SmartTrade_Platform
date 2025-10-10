package smarttrade.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smarttrade.backend.entities.CartItemEntity;
import smarttrade.backend.entities.itemEntity;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.repository.CartItemRepo;
import smarttrade.backend.repository.itemRepo;
import smarttrade.backend.repository.userRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepo cartRepo;

    public CartItemEntity addToCart(CartItemEntity cartItem) {
        cartItem.setDateAdded(LocalDateTime.now());
        return cartRepo.save(cartItem);
    }
    @Transactional
    public void removeFromCart(Long userId, Long itemId) {
        cartRepo.deleteByUser_userIdAndItem_itemId(userId, itemId);
    }

    public List<CartItemEntity> getUserCart(Long userId) {
        return cartRepo.findByUser_userId(userId);
    }
}




