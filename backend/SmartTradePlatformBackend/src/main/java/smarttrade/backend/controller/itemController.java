package smarttrade.backend.controller;

import org.modelmapper.ModelMapper;
import smarttrade.backend.Mappers.itemMapperImpl;
import smarttrade.backend.dto.itemDto;
import smarttrade.backend.entities.itemEntity;
import smarttrade.backend.service.itemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class itemController {
    private itemService itemService;
    itemMapperImpl itemMapper;
    public itemController(itemService itemService, itemMapperImpl itemMapper){
        this.itemService=itemService;
        this.itemMapper=itemMapper;

    }
    @GetMapping("/items")
    public ResponseEntity<List<itemDto>> getAllItems(){
       List<itemEntity> items= itemService.GetAllItems();
       List<itemDto> itemDtos= items.stream().map(itemMapper::mapFrom).toList();
       return new ResponseEntity<>(itemDtos, HttpStatus.OK);
    }
    @GetMapping("items/{item_id}")
    public ResponseEntity<itemDto> getItems(@PathVariable("item_id") Long item_id){
        return itemService.GetItem(item_id).map(itemEntity ->{
            itemDto itemDto= itemMapper.mapFrom(itemEntity);
            return new ResponseEntity<>(itemDto,HttpStatus.OK);
                }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @PostMapping("/items")
    public ResponseEntity<itemDto> addItem(@RequestBody itemDto item){
        itemEntity itemEntity=itemMapper.mapTo(item);
        itemDto itemDto=itemMapper.mapFrom(itemService.addItems(itemEntity));
        return new ResponseEntity<>(itemDto, HttpStatus.CREATED);
    }
    @PatchMapping("/items/{item_id}")
    public ResponseEntity<itemDto> UpdateItem(@PathVariable("item_id") Long item_id,
                                                 @RequestBody itemDto item){
        if(itemService.GetItem(item_id).isEmpty()){
            return new ResponseEntity<>(item,HttpStatus.NOT_FOUND);
        }
        itemEntity itemEntity= itemMapper.mapTo(item);
        itemDto itemDto=itemMapper.mapFrom(itemService.updateItem(item_id,itemEntity));
        return new ResponseEntity<>(itemDto, HttpStatus.OK);
    }
    @DeleteMapping("/items/{item_id}")
    public ResponseEntity deleteItem(@PathVariable("item_id") Long item_id){
        itemService.deleteItem(item_id);
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
    @PatchMapping("/items/{item_id}/availability")
    public ResponseEntity<Void> toggleAvailability(@PathVariable Long item_id,
                                                   @RequestParam boolean available) {
        itemService.updateAvailability(item_id, available);
        return ResponseEntity.ok().build();
    }



}
