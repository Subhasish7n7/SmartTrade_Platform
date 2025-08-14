package smarttrade.backend.controller;

import smarttrade.backend.entities.itemEntity;
import smarttrade.backend.service.itemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class itemController {
    private itemService itemService;
    public itemController(itemService itemService){
        this.itemService=itemService;

    }
    @GetMapping("/items")
    public ResponseEntity<List<itemEntity>> getAllItems(){
       List<itemEntity> items= itemService.GetAllItems();
       return new ResponseEntity<>(items, HttpStatus.OK);
    }
    @GetMapping("items/{item_id}")
    public ResponseEntity<itemEntity> getItems(@PathVariable("item_id") Long item_id){
        itemEntity item= itemService.GetItem(item_id).orElseThrow(
                ()-> new IllegalArgumentException("object not found"));

        return new ResponseEntity<>(item, HttpStatus.OK);
    }
    @PostMapping("/items")
    public ResponseEntity<itemEntity> addItem(itemEntity item){
        return new ResponseEntity<>(itemService.addItems(item), HttpStatus.CREATED);
    }
    @PatchMapping("/items/{item_id}")
    public ResponseEntity<itemEntity> UpdateItem(@PathVariable("item_id") Long item_id,
                                                 itemEntity item){
        if(itemService.GetItem(item_id).isEmpty()){
            return new ResponseEntity<>(item,HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(itemService.updateItem(item_id,item), HttpStatus.OK);
    }
    @DeleteMapping("/items/{item_id}")
    public ResponseEntity deleteItem(@PathVariable("item_id") Long item_id){
        itemService.deleteItem(item_id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
