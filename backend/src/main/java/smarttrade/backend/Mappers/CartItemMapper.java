package smarttrade.backend.Mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import smarttrade.backend.dto.CartItemDto;
import smarttrade.backend.entities.CartItemEntity;
import smarttrade.backend.repository.ItemRepo;
import smarttrade.backend.repository.UserRepo;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CartItemMapper {

    private final UserRepo userRepo;
    private final ItemRepo itemRepo;

    public CartItemEntity toEntity(CartItemDto dto) {
        return CartItemEntity.builder()
                .user(userRepo.findById(dto.getUserId()).orElseThrow())
                .item(itemRepo.findById(dto.getItemId()).orElseThrow())
                .quantity(dto.getQuantity())
                .build();
    }

    public CartItemDto toDto(CartItemEntity entity) {
        return new CartItemDto(
                entity.getId(),
                entity.getUser().getUserId(),
                entity.getItem().getItemId(),
                entity.getQuantity(),
                entity.getDateAdded()
        );
    }

    public List<CartItemDto> toDtoList(List<CartItemEntity> entities) {
        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    public CartItemDto createDto(Long userId, Long itemId, int quantity) {
        CartItemDto dto= new CartItemDto();
        dto.setUserId(userId);
        dto.setItemId(itemId);
        dto.setQuantity(quantity);
        return dto;
    }
}

