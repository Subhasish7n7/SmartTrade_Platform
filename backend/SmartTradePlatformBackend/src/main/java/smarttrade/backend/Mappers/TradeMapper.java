package smarttrade.backend.Mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import smarttrade.backend.dto.TradeOfferDto;
import smarttrade.backend.entities.TradeOfferEntity;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.entities.itemEntity;
import smarttrade.backend.repository.itemRepo;
import smarttrade.backend.repository.userRepo;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TradeMapper {

    private final userRepo userRepo;
    private final itemRepo itemRepo;

    public TradeOfferEntity mapToEntity(TradeOfferDto dto) {
        if (dto == null) return null;

        UserEntity sender = userRepo.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        UserEntity receiver = userRepo.findById(dto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        List<itemEntity> senderItems = itemRepo.findAllById(dto.getSenderItemIds());
        List<itemEntity> receiverItems = itemRepo.findAllById(dto.getReceiverItemIds());

        return TradeOfferEntity.builder()
                .createdBy(sender)
                .senderItems(senderItems)
                .receiverItems(receiverItems)
                .build();
    }

    public TradeOfferDto mapFromEntity(TradeOfferEntity entity) {
        if (entity == null) return null;

        return TradeOfferDto.builder()
                .senderId(entity.getSender().getUserId())
                .receiverId(entity.getReceiver().getUserId())
                .senderItemIds(
                        entity.getSenderItems().stream()
                                .map(itemEntity::getItemId)
                                .collect(Collectors.toList()))
                .receiverItemIds(
                        entity.getReceiverItems().stream()
                                .map(itemEntity::getItemId)
                                .collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .status(entity.getTrade().getStatus().name())
                .build();
    }
}

