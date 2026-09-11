package smarttrade.backend.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import smarttrade.backend.Mappers.ItemMapper;
import smarttrade.backend.dto.item.*;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
public class ItemController {
    private final ItemService itemService;
    private final ItemMapper itemMapper;

    public ItemController(ItemService itemService, ItemMapper itemMapper){
        this.itemService = itemService;
        this.itemMapper = itemMapper;
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemResponse>> getAllItems(){
        List<ItemEntity> items = itemService.GetAllItems();
        List<ItemResponse> ItemResponses = items.stream().map(itemMapper::mapFrom).toList();
        return new ResponseEntity<>(ItemResponses, HttpStatus.OK);
    }
    @GetMapping("/items/user/{userId}")
    public ResponseEntity<List<ItemResponse>> getInventory(@PathVariable Long userId){
        List<ItemEntity> myItems= itemService.getInventory(userId);
        List<ItemResponse> Inventory= myItems.stream().map(itemMapper::mapFrom).toList();
        return ResponseEntity.ok(Inventory);
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable("itemId") Long itemId){
        return itemService.GetItem(itemId).map(itemEntity ->{
            ItemResponse ItemResponse = itemMapper.mapFrom(itemEntity);
            return new ResponseEntity<>(ItemResponse, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/items")
    public ResponseEntity<ItemResponse> addItem(@Valid @RequestBody CreateItemRequest request) {
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

    @PostMapping("/items/search")
    public ResponseEntity<ItemSearchResponse> searchItems(@RequestBody ItemSearchRequest request
    , HttpServletRequest httpRequest) {
        System.out.println("SEARCH HIT");
        System.out.println("CSRF HEADER = "
                + httpRequest.getHeader("X-XSRF-TOKEN"));
        return ResponseEntity.ok(itemService.searchItems(request));
    }

    @PatchMapping("/items/{itemId}/availability")
    public ResponseEntity<Void> toggleAvailability(@PathVariable Long itemId,
                                                   @RequestParam boolean available) {
        itemService.updateAvailability(itemId, available);
        return ResponseEntity.ok().build();
    }
}

