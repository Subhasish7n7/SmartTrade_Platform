package smarttrade.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.CartItemEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepo extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findByUser_userId(Long userId);
    void deleteByUser_userIdAndItem_itemId(Long userId, Long itemId);
    Optional<CartItemEntity> findByUser_UserIdAndItem_ItemId(Long userId, Long itemId);
}


