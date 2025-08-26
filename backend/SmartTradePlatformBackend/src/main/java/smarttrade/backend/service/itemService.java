package smarttrade.backend.service;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
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

    public List<itemEntity> getNearbyItems(double lat, double lng, double radiusKm) {
        double radiusMeters = radiusKm * 1000;
        return itemRepo.findNearbyItems(lat, lng, radiusMeters);
    }


    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<itemEntity> searchItems(String category, List<String> labels, String name) {
        List<itemEntity> allItems = itemRepo.findAll();

        return allItems.stream()
                .filter(item -> item.isAvailable())
                .filter(item -> category == null || item.getCategory().equalsIgnoreCase(category))
                .filter(item -> name == null || item.getItem_name().toLowerCase().contains(name.toLowerCase()))
                .filter(item -> {
                    if (labels == null || labels.isEmpty()) return true;
                    List<String> itemLabels = item.getLabels();
                    return itemLabels != null && itemLabels.containsAll(labels);
                })
                .collect(Collectors.toList());
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
