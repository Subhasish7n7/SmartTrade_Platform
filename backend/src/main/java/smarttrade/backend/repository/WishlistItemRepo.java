package smarttrade.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.WishlistItemEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepo extends JpaRepository<WishlistItemEntity, Long> {
    void deleteByUser_UserIdAndItem_ItemId(Long userId, Long itemId);

    List<WishlistItemEntity> findByUser_UserId(Long userId);

    List<WishlistItemEntity> findByUser_UserIdOrderByDateAddedDesc(Long userId);

    Optional<WishlistItemEntity> findFirstByUser_UserIdAndItem_ItemId(Long userId, Long itemId);
}
