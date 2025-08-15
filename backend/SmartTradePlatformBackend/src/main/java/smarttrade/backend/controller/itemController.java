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
        itemEntity item= itemService.GetItem(item_id).orElseThrow(
                ()-> new IllegalArgumentException("object not found"));

        return new ResponseEntity<>(itemMapper.mapFrom(item), HttpStatus.OK);
    }
    @PostMapping("/items")
    public ResponseEntity<itemDto> addItem(@RequestBody itemDto item){
        itemEntity itemEntity=itemMapper.mapTo(item);
        itemDto itemDto=itemMapper.mapFrom(itemService.addItems(itemEntity));
        return new ResponseEntity<>(itemDto, HttpStatus.CREATED);
    }
    @PatchMapping("/items/{item_id}")
    public ResponseEntity<itemDto> UpdateItem(@PathVariable("item_id") Long item_id,
                                                 itemDto item){
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
}
