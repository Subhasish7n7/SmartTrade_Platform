package smarttrade.backend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import smarttrade.backend.entities.WishlistItemEntity;
import smarttrade.backend.repository.WishlistItemRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepo wishlistRepo;

    public WishlistItemEntity addToWishlist(WishlistItemEntity wishlistItem) {
        wishlistItem.setDateAdded(LocalDateTime.now());
        return wishlistRepo.save(wishlistItem);
    }
    @Transactional
    public void removeFromWishlist(Long userId, Long itemId) {
        wishlistRepo.deleteByUser_UserIdAndItem_ItemId(userId, itemId);
    }

    public List<WishlistItemEntity> getUserWishlist(Long userId) {
        return wishlistRepo.findByUser_UserId(userId);
    }
}



