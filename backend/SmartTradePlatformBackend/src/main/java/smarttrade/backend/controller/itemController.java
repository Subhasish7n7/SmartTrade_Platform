package smarttrade.backend.controller;


import smarttrade.backend.Mappers.itemMapperImpl;
import smarttrade.backend.dto.itemDto;
import smarttrade.backend.entities.itemEntity;
import smarttrade.backend.service.itemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
public class itemController {
    private final itemService itemService;
    private final itemMapperImpl itemMapper;

    public itemController(itemService itemService, itemMapperImpl itemMapper){
        this.itemService = itemService;
        this.itemMapper = itemMapper;
    }

    @GetMapping("/items")
    public ResponseEntity<List<itemDto>> getAllItems(){
        List<itemEntity> items = itemService.GetAllItems();
        List<itemDto> itemDtos = items.stream().map(itemMapper::mapFrom).toList();
        return new ResponseEntity<>(itemDtos, HttpStatus.OK);
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<itemDto> getItem(@PathVariable("itemId") Long itemId){
        return itemService.GetItem(itemId).map(itemEntity ->{
            itemDto itemDto = itemMapper.mapFrom(itemEntity);
            return new ResponseEntity<>(itemDto, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/items")
    public ResponseEntity<itemDto> addItem(@RequestBody itemDto item) {
        System.out.println(" Received item DTO: " + item);
        itemEntity itemEntity = itemMapper.mapTo(item);
        System.out.println("Mapped entity: " + itemEntity);
        itemDto itemDto = itemMapper.mapFrom(itemService.addItems(itemEntity));
        return new ResponseEntity<>(itemDto, HttpStatus.CREATED);
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<itemDto> updateItem(@PathVariable("itemId") Long itemId,
                                              @RequestBody itemDto item){
        if(itemService.GetItem(itemId).isEmpty()){
            return new ResponseEntity<>(item, HttpStatus.NOT_FOUND);
        }
        itemEntity itemEntity = itemMapper.mapTo(item);
        itemDto itemDto = itemMapper.mapFrom(itemService.updateItem(itemId, itemEntity));
        return new ResponseEntity<>(itemDto, HttpStatus.OK);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable("itemId") Long itemId){
        itemService.deleteItem(itemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/items/nearby")
    public ResponseEntity<List<itemDto>> getNearbyItems(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radiusKm
    ) {
        List<itemDto> items = itemService.getNearbyItems(lat, lng, radiusKm)
                .stream().map(itemMapper::mapFrom).toList();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/search")
    public ResponseEntity<List<itemDto>> searchItems(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> labels,
            @RequestParam(required = false) String name
    ) {
        List<itemDto> items = itemService.searchItems(category, labels, name)
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

