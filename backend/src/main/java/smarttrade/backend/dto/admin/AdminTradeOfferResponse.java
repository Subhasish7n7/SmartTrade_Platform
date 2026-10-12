package smarttrade.backend.dto.admin;

import java.time.LocalDateTime;
import java.util.List;

public record AdminTradeOfferResponse(
        Long offerId,
        Long senderId,
        String senderName,
        Long receiverId,
        String receiverName,
        Double cashAdjustment,
        LocalDateTime createdAt,
        List<AdminTradeItemResponse> senderItems,
        List<AdminTradeItemResponse> receiverItems
) {}