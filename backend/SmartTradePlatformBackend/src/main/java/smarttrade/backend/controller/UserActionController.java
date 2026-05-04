package smarttrade.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.Mappers.CartItemMapper;
import smarttrade.backend.Mappers.WishListItemMapper;
import smarttrade.backend.dto.CartItemDto;
import smarttrade.backend.dto.WishlistItemDto;
import smarttrade.backend.entities.CartItemEntity;
import smarttrade.backend.entities.WishlistItemEntity;
import smarttrade.backend.service.CartService;
import smarttrade.backend.service.WishlistService;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserActionController {

    private final CartService cartService;
    private final WishlistService wishlistService;
    private final CartItemMapper cartItemMapper;
    private final WishListItemMapper wishlistItemMapper;

    // ========== CART ==========

    @PostMapping("/{userId}/cart/{itemId}")
    public ResponseEntity<CartItemDto> addToCart(@PathVariable Long userId,
                                                 @PathVariable Long itemId,
                                                 @RequestParam(defaultValue = "1") int quantity) {
        CartItemDto dto = cartItemMapper.createDto(userId, itemId,quantity);
        CartItemEntity entity = cartItemMapper.toEntity(dto);
        CartItemEntity savedEntity = cartService.addToCart(entity);
        CartItemDto responseDto = cartItemMapper.toDto(savedEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @DeleteMapping("/{userId}/cart/{itemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long userId,
                                               @PathVariable Long itemId) {
        cartService.removeFromCart(userId, itemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{userId}/cart")
    public ResponseEntity<List<CartItemDto>> getCart(@PathVariable Long userId) {
        List<CartItemEntity> cartEntities = cartService.getUserCart(userId);
        List<CartItemDto> cartDtos = cartItemMapper.toDtoList(cartEntities);
        return ResponseEntity.ok(cartDtos);
    }

    // ========== WISHLIST ==========

    @PostMapping("/{userId}/wishlist/{itemId}")
    public ResponseEntity<WishlistItemDto> addToWishlist(@PathVariable Long userId,
                                                         @PathVariable Long itemId) {
        WishlistItemDto dto = wishlistItemMapper.createDto(userId,itemId);
        WishlistItemEntity entity = wishlistItemMapper.toEntity(dto);
        WishlistItemEntity savedEntity = wishlistService.addToWishlist(entity);
        WishlistItemDto responseDto = wishlistItemMapper.toDto(savedEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @DeleteMapping("/{userId}/wishlist/{itemId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long userId,
                                                   @PathVariable Long itemId) {
        wishlistService.removeFromWishlist(userId, itemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{userId}/wishlist")
    public ResponseEntity<List<WishlistItemDto>> getWishlist(@PathVariable Long userId) {
        List<WishlistItemEntity> wishlistEntities = wishlistService.getUserWishlist(userId);
        List<WishlistItemDto> wishlistDtos = wishlistItemMapper.toDtoList(wishlistEntities);
        return ResponseEntity.ok(wishlistDtos);
    }
}


