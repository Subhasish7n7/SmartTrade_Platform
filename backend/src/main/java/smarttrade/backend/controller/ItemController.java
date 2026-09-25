package smarttrade.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.Mappers.ItemMapper;
import smarttrade.backend.dto.item.*;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.service.ItemService;
import smarttrade.backend.service.pricing.PricingRefreshResponse;

import java.util.List;

@RestController
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    public ItemController(ItemService itemService, ItemMapper itemMapper) {
        this.itemService = itemService;
        this.itemMapper = itemMapper;
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        return ResponseEntity.ok(
                itemService.GetAllItems()
                        .stream()
                        .map(itemMapper::mapFrom)
                        .toList()
        );
    }
    @GetMapping("/items/recent")
    public ResponseEntity<List<ItemResponse>> getRecentMarketplaceItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                itemService.getRecentMarketplaceItems(page,size)
                        .stream()
                        .map(itemMapper::mapFrom)
                        .toList()
        );
    }

    @GetMapping("/items/user/{userId}")
    public ResponseEntity<List<ItemResponse>> getInventory(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                itemService.getInventory(userId)
                        .stream()
                        .map(itemMapper::mapFrom)
                        .toList()
        );
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable Long itemId) {
        return itemService.GetItem(itemId)
                .map(entity -> ResponseEntity.ok(itemMapper.mapFrom(entity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/items")
    public ResponseEntity<ItemResponse> addItem(
            @Valid @RequestBody CreateItemRequest request
    ) {
        ItemEntity item = itemMapper.fromCreateRequest(request);
        ItemResponse response = itemMapper.mapFrom(itemService.addItems(item));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemRequest request
    ) {
        ItemResponse response = itemMapper.mapFrom(
                itemService.updateItem(itemId, request)
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long itemId
    ) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/items/search")
    public ResponseEntity<ItemSearchResponse> searchItems(
            @RequestBody ItemSearchRequest request
    ) {
        return ResponseEntity.ok(itemService.searchItems(request));
    }

    @PostMapping("/items/{itemId}/price/refresh")
    public ResponseEntity<PricingRefreshResponse> refreshItemPrice(
            @PathVariable Long itemId
    ) {
        PricingRefreshResponse response = itemService.refreshItemPrice(itemId);

        return switch (response.outcome()) {
            case STARTED, ALREADY_RUNNING ->
                    ResponseEntity.accepted().body(response);
            case ALREADY_FRESH ->
                    ResponseEntity.ok(response);
            case NOT_FOUND ->
                    ResponseEntity.notFound().build();
        };
    }

    @PatchMapping("/items/{itemId}/availability")
    public ResponseEntity<Void> toggleAvailability(
            @PathVariable Long itemId,
            @RequestParam boolean available
    ) {
        itemService.updateAvailability(itemId, available);
        return ResponseEntity.ok().build();
    }
}