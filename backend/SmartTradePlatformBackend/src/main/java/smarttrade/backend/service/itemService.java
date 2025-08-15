package smarttrade.backend.service;

import smarttrade.backend.entities.itemEntity;
import smarttrade.backend.repository.itemRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class itemService {
    itemRepo itemRepo;
    itemService(itemRepo itemRepo){
        this.itemRepo=itemRepo;
    }
    public List<itemEntity> GetAllItems() {
        return itemRepo.findAll();
    }

    public Optional<itemEntity> GetItem(Long item_id) {
        return itemRepo.findById(item_id);
    }

    public itemEntity addItems(itemEntity item) {
        return itemRepo.save(item);
    }

    public itemEntity updateItem(Long itemId, itemEntity item) {
        item.setItem_id(itemId);
        return itemRepo.save(item);
    }

    public void deleteItem(Long itemId) {
        itemRepo.deleteById(itemId);
    }
}
