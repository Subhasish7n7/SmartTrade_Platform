package smarttrade.backend.service;


import smarttrade.backend.Mappers.ItemMapperImpl;
import smarttrade.backend.dto.UpdateItemRequest;
import smarttrade.backend.entities.ItemEntity;
import smarttrade.backend.exceptions.ForbiddenOperationException;
import smarttrade.backend.repository.ItemRepo;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.security.AuthenticatedUserService;

import java.util.List;
import java.util.Optional;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    private final ItemRepo itemRepo;
    private final ItemMapperImpl itemMapper;
    private final AuthenticatedUserService authenticatedUserService;
    public List<ItemEntity> GetAllItems() {
        
        return itemRepo.findAll();
    }

    public Optional<ItemEntity> GetItem(Long itemId) {
        return itemRepo.findById(itemId);
    }

    @Transactional
    public ItemEntity addItems(ItemEntity item) {

        UserEntity currentUser =
                authenticatedUserService.getCurrentUser();

    /*
        NEVER trust frontend ownership.
    */
        item.setUser(currentUser);

        log.info(
                "User {} creating item {}",
                currentUser.getEmail(),
                item.getItem_name()
        );

        return itemRepo.save(item);
    }

    @Transactional
    public ItemEntity updateItem(
            Long itemId,
            UpdateItemRequest request
    ){

        UserEntity currentUser =
                authenticatedUserService.getCurrentUser();

        ItemEntity existingItem = itemRepo
                .findByItemIdAndUser_UserId(
                        itemId,
                        currentUser.getUserId()
                )
                .orElseThrow(() ->
                        new ForbiddenOperationException("Item not owned by user"));

        itemMapper.updateEntityFromRequest(
                request,
                existingItem
        );

        log.info(
                "User {} updated item {}",
                currentUser.getEmail(),
                itemId
        );

        return itemRepo.save(existingItem);
    }
    @Transactional
    public void deleteItem(Long itemId) {

        UserEntity currentUser =
                authenticatedUserService.getCurrentUser();

        ItemEntity item = itemRepo
                .findByItemIdAndUser_UserId(
                        itemId,
                        currentUser.getUserId()
                )
                .orElseThrow(() ->
                        new ForbiddenOperationException("Item not owned by user"));

        log.info(
                "User {} deleted item {}",
                currentUser.getEmail(),
                itemId
        );

        itemRepo.delete(item);
    }

    public List<ItemEntity> getNearbyItems(double lat, double lng, double radiusKm) {
        double radiusMeters = radiusKm * 1000;
        return itemRepo.findNearbyItems(lat, lng, radiusMeters);
    }

    public List<ItemEntity> searchItems(String category, List<String> labels, String name) {
        return itemRepo.searchItems(category, name);
    }

    @Transactional
    public void updateAvailability(Long itemId, boolean available) {

        UserEntity currentUser =
                authenticatedUserService.getCurrentUser();

        ItemEntity item = itemRepo
                .findByItemIdAndUser_UserId(
                        itemId,
                        currentUser.getUserId()
                )
                .orElseThrow(() ->
                        new ForbiddenOperationException("Item not owned by user"));

        item.setAvailable(available);

        log.info(
                "User {} changed availability for item {} to {}",
                currentUser.getEmail(),
                itemId,
                available
        );

        itemRepo.save(item);
    }

}
