package smarttrade.backend.Mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import smarttrade.backend.dto.WishlistItemDto;
import smarttrade.backend.entities.WishlistItemEntity;
import smarttrade.backend.repository.itemRepo;
import smarttrade.backend.repository.userRepo;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WishListItemMapper {

    private final userRepo userRepo;
    private final itemRepo itemRepo;

    public WishlistItemEntity toEntity(WishlistItemDto dto) {
        return WishlistItemEntity.builder()
                .user(userRepo.findById(dto.getUserId()).orElseThrow())
                .item(itemRepo.findById(dto.getItemId()).orElseThrow())
                .build();
    }

    public WishlistItemDto toDto(WishlistItemEntity entity) {
        return new WishlistItemDto(
                entity.getId(),
                entity.getUser().getUserId(),
                entity.getItem().getItemId(),
                entity.getDateAdded()
        );
    }

    public List<WishlistItemDto> toDtoList(List<WishlistItemEntity> entities) {
        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    public WishlistItemDto createDto(Long userId, Long itemId) {
        WishlistItemDto dto= new WishlistItemDto();
        dto.setUserId(userId);
        dto.setItemId(itemId);
        return dto;
    }
}

