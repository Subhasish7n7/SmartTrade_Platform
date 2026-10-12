package smarttrade.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import smarttrade.backend.Mappers.WishListItemMapper;
import smarttrade.backend.dto.WishlistItemDto;
import smarttrade.backend.entities.WishlistItemEntity;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.WishlistItemRepo;
import smarttrade.backend.repository.item.ItemRepo;
import smarttrade.backend.security.AuthenticatedUserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepo wishlistRepo;
    private final AuthenticatedUserService authenticatedUserService;
    private final WishListItemMapper wishlistMapper;
    private final ItemRepo itemRepo;

    /**
     * Self-scoped endpoint: the user identity always comes from authentication.
     */
    @Transactional(readOnly = true)
    public List<WishlistItemDto> getCurrentUserWishlist() {
        Long currentUserId = currentUser().getUserId();
        return getUserWishlist(currentUserId).stream()
                .map(wishlistMapper::toDto)
                .toList();
    }

    /**
     * Idempotent add: repeated clicks return the existing row instead of adding duplicates.
     */
    @Transactional
    public WishlistItemDto addToCurrentUserWishlist(Long itemId) {
        if (itemId == null || itemId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid item ID is required.");
        }

        UserEntity user = currentUser();
        WishlistItemEntity existing = wishlistRepo
                .findFirstByUser_UserIdAndItem_ItemId(user.getUserId(), itemId)
                .orElse(null);
        if (existing != null) {
            return wishlistMapper.toDto(existing);
        }

        if (!itemRepo.existsById(itemId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found.");
        }

        WishlistItemEntity entity = wishlistMapper.toEntity(
                wishlistMapper.createDto(user.getUserId(), itemId)
        );
        entity.setDateAdded(LocalDateTime.now());
        return wishlistMapper.toDto(wishlistRepo.save(entity));
    }

    @Transactional
    public void removeFromCurrentUserWishlist(Long itemId) {
        if (itemId == null || itemId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid item ID is required.");
        }
        removeFromWishlist(currentUser().getUserId(), itemId);
    }

    /**
     * Legacy service method retained for any existing internal callers.
     */
    @Transactional
    public WishlistItemEntity addToWishlist(WishlistItemEntity wishlistItem) {
        UserEntity user = currentUser();
        if (wishlistItem == null
                || wishlistItem.getUser() == null
                || wishlistItem.getUser().getUserId() == null
                || !user.getUserId().equals(wishlistItem.getUser().getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "A wishlist entry can only be created for the authenticated user."
            );
        }
        if (wishlistItem.getDateAdded() == null) {
            wishlistItem.setDateAdded(LocalDateTime.now());
        }
        return wishlistRepo.save(wishlistItem);
    }

    /**
     * Legacy service method retained for compatibility but restricted to the authenticated user.
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long itemId) {
        requireCurrentUser(userId);
        if (itemId == null || itemId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid item ID is required.");
        }
        wishlistRepo.deleteByUser_UserIdAndItem_ItemId(userId, itemId);
    }

    /**
     * Legacy service method retained for compatibility but restricted to the authenticated user.
     */
    @Transactional(readOnly = true)
    public List<WishlistItemEntity> getUserWishlist(Long userId) {
        requireCurrentUser(userId);
        return wishlistRepo.findByUser_UserIdOrderByDateAddedDesc(userId);
    }

    private UserEntity currentUser() {
        return authenticatedUserService.getCurrentUser();
    }

    private void requireCurrentUser(Long requestedUserId) {
        UserEntity user = currentUser();
        if (!user.getUserId().equals(requestedUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only access your own wishlist."
            );
        }
    }
}
