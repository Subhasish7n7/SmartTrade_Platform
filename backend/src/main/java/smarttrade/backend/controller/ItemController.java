package smarttrade.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.Mappers.ItemMapper;
import smarttrade.backend.dto.item.*;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.security.AuthenticatedUserService;
import smarttrade.backend.service.item.ItemService;
import smarttrade.backend.service.item.PublicItemVisibilityService;
import smarttrade.backend.service.pricing.PricingRefreshResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final PublicItemVisibilityService publicItemVisibilityService;
    private final AuthenticatedUserService authenticatedUserService;

    @GetMapping("/items")
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        return ResponseEntity.ok(publicItemVisibilityService.mapPublicItems(itemService.GetAllItems()));
    }

    @GetMapping("/items/recent")
    public ResponseEntity<List<ItemResponse>> getRecentMarketplaceItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(publicItemVisibilityService.mapPublicItems(
                itemService.getRecentMarketplaceItems(page, size)));
    }


    @GetMapping("/items/me/inventory")
    public ResponseEntity<List<ItemResponse>> getMyInventory() {
        Long currentUserId = authenticatedUserService.getCurrentUser().getUserId();

        return ResponseEntity.ok(
                publicItemVisibilityService.mapOwnerInventory(
                        itemService.getInventory(currentUserId)
                )
        );
    }

    @GetMapping("/items/user/{userId}")
    public ResponseEntity<List<ItemResponse>> getInventory(@PathVariable Long userId) {
        if (!publicItemVisibilityService.userExists(userId)) {
            return ResponseEntity.notFound().build();
        }
        if (!publicItemVisibilityService.canViewPublicInventory(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(publicItemVisibilityService.mapPublicInventory(
                userId, itemService.getInventory(userId)));
    }


    @GetMapping("/items/{itemId}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable Long itemId) {
        return itemService.GetItem(itemId)
                .map(publicItemVisibilityService::mapPublicItem)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/items")
    public ResponseEntity<ItemResponse> addItem(@Valid @RequestBody CreateItemRequest request) {
        ItemEntity item = itemMapper.fromCreateRequest(request);
        ItemResponse response = itemMapper.mapFrom(itemService.addItems(item));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable Long itemId,
                                                   @Valid @RequestBody UpdateItemRequest request) {
        return ResponseEntity.ok(itemMapper.mapFrom(itemService.updateItem(itemId, request)));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/items/search")
    public ResponseEntity<ItemSearchResponse> searchItems(@RequestBody ItemSearchRequest request) {
        ItemSearchResponse response = itemService.searchItems(request);
        publicItemVisibilityService.redactLocations(response.getItems());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items/{itemId}/price/refresh")
    public ResponseEntity<PricingRefreshResponse> refreshItemPrice(@PathVariable Long itemId) {
        PricingRefreshResponse response = itemService.refreshItemPrice(itemId);
        return switch (response.outcome()) {
            case STARTED, ALREADY_RUNNING -> ResponseEntity.accepted().body(response);
            case ALREADY_FRESH -> ResponseEntity.ok(response);
            case NOT_FOUND -> ResponseEntity.notFound().build();
        };
    }

    @PatchMapping("/items/{itemId}/availability")
    public ResponseEntity<Void> toggleAvailability(@PathVariable Long itemId,
                                                   @RequestParam boolean available) {
        itemService.updateAvailability(itemId, available);
        return ResponseEntity.ok().build();
    }
}
