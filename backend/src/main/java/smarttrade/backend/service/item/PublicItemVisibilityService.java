package smarttrade.backend.service.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttrade.backend.Mappers.ItemMapper;
import smarttrade.backend.dto.item.ItemResponse;
import smarttrade.backend.entities.item.ItemEntity;
import smarttrade.backend.entities.user.*;
import smarttrade.backend.repository.user.UserPreferenceRepo;
import smarttrade.backend.repository.user.UserRepo;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicItemVisibilityService {
    private final ItemMapper itemMapper;
    private final UserRepo userRepo;
    private final UserPreferenceRepo preferenceRepo;

    @Transactional(readOnly = true)
    public boolean canViewPublicInventory(Long userId) {
        UserEntity user = userRepo.findById(userId).orElse(null);
        return user != null && preferenceFor(user).getInventoryVisibility() == InventoryVisibility.PUBLIC;
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> mapPublicInventory(Long userId, List<ItemEntity> entities) {
        if (!canViewPublicInventory(userId)) return List.of();
        return mapPublicItems(entities);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> mapPublicItems(List<ItemEntity> entities) {
        Map<Long, UserPreferenceEntity> preferences = preferencesForEntities(entities);
        return entities.stream()
                .filter(item -> item.isAvailable() || pref(preferences, item.getUser().getUserId()).isShowUnavailableListings())
                .map(item -> redactLocation(itemMapper.mapFrom(item), pref(preferences, item.getUser().getUserId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> mapOwnerInventory(List<ItemEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(itemMapper::mapFrom)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        return userId != null && userRepo.existsById(userId);
    }

    @Transactional(readOnly = true)
    public ItemResponse mapPublicItem(ItemEntity entity) {
        UserPreferenceEntity p = preferenceFor(entity.getUser());
        if (!entity.isAvailable() && !p.isShowUnavailableListings()) return null;
        return redactLocation(itemMapper.mapFrom(entity), p);
    }

    @Transactional(readOnly = true)
    public List<ItemEntity> filterUnavailableListings(List<ItemEntity> entities) {
        Map<Long, UserPreferenceEntity> preferences = preferencesForEntities(entities);
        return entities.stream()
                .filter(item -> item.isAvailable() || pref(preferences, item.getUser().getUserId()).isShowUnavailableListings())
                .toList();
    }

    @Transactional(readOnly = true)
    public void redactLocations(List<ItemResponse> responses) {
        if (responses == null || responses.isEmpty()) return;
        Set<Long> userIds = responses.stream()
                .filter(Objects::nonNull)
                .map(ItemResponse::getSeller)
                .filter(Objects::nonNull)
                .map(seller -> seller.getUserId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, UserPreferenceEntity> preferences = preferencesForIds(userIds);
        for (ItemResponse response : responses) {
            if (response == null || response.getSeller() == null || response.getSeller().getUserId() == null) continue;
            redactLocation(response, pref(preferences, response.getSeller().getUserId()));
        }
    }

    private Map<Long, UserPreferenceEntity> preferencesForEntities(List<ItemEntity> entities) {
        if (entities == null || entities.isEmpty()) return Map.of();
        Map<Long, UserEntity> owners = entities.stream()
                .filter(Objects::nonNull)
                .map(ItemEntity::getUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserEntity::getUserId, Function.identity(), (a, b) -> a));
        return preferencesForIdsAndOwners(owners);
    }

    private Map<Long, UserPreferenceEntity> preferencesForIdsAndOwners(Map<Long, UserEntity> owners) {
        if (owners.isEmpty()) return Map.of();
        Map<Long, UserPreferenceEntity> result = preferencesForIds(owners.keySet());
        owners.forEach((id, owner) -> result.computeIfAbsent(id, ignored -> defaultsFor(owner)));
        return result;
    }

    private Map<Long, UserPreferenceEntity> preferencesForIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return new HashMap<>();
        return preferenceRepo.findAllByUser_UserIdIn(userIds).stream()
                .collect(Collectors.toMap(p -> p.getUser().getUserId(), Function.identity(), (a, b) -> a));
    }

    private UserPreferenceEntity preferenceFor(UserEntity user) {
        return preferenceRepo.findByUser_UserId(user.getUserId()).orElseGet(() -> defaultsFor(user));
    }

    private UserPreferenceEntity defaultsFor(UserEntity user) {
        return UserPreferenceEntity.builder().user(user).build();
    }

    private UserPreferenceEntity pref(Map<Long, UserPreferenceEntity> values, Long userId) {
        return values.getOrDefault(userId, UserPreferenceEntity.builder().build());
    }

    private ItemResponse redactLocation(ItemResponse response, UserPreferenceEntity preference) {
        if (response == null) return null;
        switch (preference.getLocationVisibility()) {
            case EXACT -> {
            }
            case CITY -> {
                response.setLatitude(null);
                response.setLongitude(null);
                response.setLocality(null);
            }
            case HIDDEN -> {
                response.setLatitude(null);
                response.setLongitude(null);
                response.setLocality(null);
                response.setCity(null);
                response.setState(null);
            }
        }
        return response;
    }
}
