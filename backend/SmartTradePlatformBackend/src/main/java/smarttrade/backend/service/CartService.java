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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepo cartRepo;

    public CartItemEntity addToCart(CartItemEntity cartItem) {

        Optional<CartItemEntity> existing =
                cartRepo.findByUser_UserIdAndItem_ItemId(
                        cartItem.getUser().getUserId(),
                        cartItem.getItem().getItemId()
                );

        if (existing.isPresent()) {
            CartItemEntity entity = existing.get();
            entity.setQuantity(entity.getQuantity() + cartItem.getQuantity());
            return cartRepo.save(entity);
        }

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




