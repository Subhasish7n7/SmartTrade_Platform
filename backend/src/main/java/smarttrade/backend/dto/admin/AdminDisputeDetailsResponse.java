package smarttrade.backend.dto.admin;

public record AdminDisputeDetailsResponse(
        AdminDisputeSummaryResponse dispute,
        String description,
        String resolutionNotes,
        AdminTradeSummaryResponse trade
) {}