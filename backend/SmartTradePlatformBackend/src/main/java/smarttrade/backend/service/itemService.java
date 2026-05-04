package smarttrade.backend.service;


import smarttrade.backend.entities.itemEntity;
import smarttrade.backend.repository.itemRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class itemService {
    itemRepo itemRepo;
    itemService(itemRepo itemRepo){
        this.itemRepo=itemRepo;
    }
    public List<itemEntity> GetAllItems() {
        
        return itemRepo.findAll();
    }

    public Optional<itemEntity> GetItem(Long itemId) {
        return itemRepo.findById(itemId);
    }

    public itemEntity addItems(itemEntity item) {
        return itemRepo.save(item);
    }


    public itemEntity updateItem(Long itemId, itemEntity item) {
        item.setItemId(itemId);
        return itemRepo.save(item);
    }

    public void deleteItem(Long itemId) {
        itemRepo.deleteById(itemId);
    }

    public List<itemEntity> getNearbyItems(double lat, double lng, double radiusKm) {
        double radiusMeters = radiusKm * 1000;
        return itemRepo.findNearbyItems(lat, lng, radiusMeters);
    }

    public List<itemEntity> searchItems(String category, List<String> labels, String name) {
        return itemRepo.searchItems(category, name);
    }
    public void updateAvailability(Long itemId, boolean available) {
        Optional<itemEntity> optionalItem = itemRepo.findById(itemId);
        if (optionalItem.isEmpty()) {
            throw new IllegalArgumentException("Item not found");
        }

        itemEntity item = optionalItem.get();
        item.setAvailable(available);
        itemRepo.save(item);
    }

}
