package smarttrade.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import smarttrade.backend.dto.WishlistItemDto;
import smarttrade.backend.service.WishlistService;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<WishlistItemDto>> getMyWishlist() {
        return ResponseEntity.ok(wishlistService.getCurrentUserWishlist());
    }

    @PostMapping("/{itemId}")
    public ResponseEntity<WishlistItemDto> addToMyWishlist(@PathVariable Long itemId) {
        return ResponseEntity.ok(wishlistService.addToCurrentUserWishlist(itemId));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeFromMyWishlist(@PathVariable Long itemId) {
        wishlistService.removeFromCurrentUserWishlist(itemId);
        return ResponseEntity.noContent().build();
    }
}
