package smarttrade.backend.controller;


import smarttrade.backend.Mappers.ItemMapperImpl;
import smarttrade.backend.dto.CreateItemRequest;
import smarttrade.backend.dto.ItemResponse;
import smarttrade.backend.dto.UpdateItemRequest;
import smarttrade.backend.entities.ItemEntity;
import smarttrade.backend.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
public class ItemController {
    private final ItemService itemService;
    private final ItemMapperImpl itemMapper;

    public ItemController(ItemService itemService, ItemMapperImpl itemMapper){
        this.itemService = itemService;
        this.itemMapper = itemMapper;
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemResponse>> getAllItems(){
        List<ItemEntity> items = itemService.GetAllItems();
        List<ItemResponse> ItemResponses = items.stream().map(itemMapper::mapFrom).toList();
        return new ResponseEntity<>(ItemResponses, HttpStatus.OK);
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable("itemId") Long itemId){
        return itemService.GetItem(itemId).map(itemEntity ->{
            ItemResponse ItemResponse = itemMapper.mapFrom(itemEntity);
            return new ResponseEntity<>(ItemResponse, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/items")
    public ResponseEntity<ItemResponse> addItem(@RequestBody CreateItemRequest request) {
        ItemEntity itemEntity = itemMapper.fromCreateRequest(request);
        ItemResponse itemResponse = itemMapper.mapFrom(itemService.addItems(itemEntity));
        return new ResponseEntity<>(itemResponse, HttpStatus.CREATED);
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ItemResponse> updateItem(@PathVariable("itemId") Long itemId,
                                                   @RequestBody UpdateItemRequest request) {
        ItemEntity updatedItem = itemService.updateItem(itemId, request);
        ItemResponse response = itemMapper.mapFrom(updatedItem);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable("itemId") Long itemId){
        itemService.deleteItem(itemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/items/nearby")
    public ResponseEntity<List<ItemResponse>> getNearbyItems(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radiusKm
    ) {
        List<ItemResponse> items = itemService.getNearbyItems(lat, lng, radiusKm)
                .stream().map(itemMapper::mapFrom).toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/search")
    public ResponseEntity<List<ItemResponse>> searchItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> labels,
            @RequestParam(required = false) String name
    ) {
        List<ItemResponse> items = itemService.searchItems(category, labels, name)
                .stream().map(itemMapper::mapFrom).toList();
        return ResponseEntity.ok(items);
    }

    @PatchMapping("/items/{itemId}/availability")
    public ResponseEntity<Void> toggleAvailability(@PathVariable Long itemId,
                                                   @RequestParam boolean available) {
        itemService.updateAvailability(itemId, available);
        return ResponseEntity.ok().build();
    }
}

