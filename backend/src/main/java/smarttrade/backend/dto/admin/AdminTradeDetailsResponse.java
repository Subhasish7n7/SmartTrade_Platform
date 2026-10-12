package smarttrade.backend.dto.admin;

import java.util.List;

public record AdminTradeDetailsResponse(
        AdminTradeSummaryResponse trade,
        List<AdminTradeOfferResponse> offerHistory,
        AdminDisputeSummaryResponse latestDispute
) {
}