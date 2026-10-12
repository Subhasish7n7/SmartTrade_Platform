package smarttrade.backend.dto.trade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import smarttrade.backend.entities.trade.DisputeReason;

public record CreateDisputeRequest(
        @NotNull DisputeReason reason,
        @NotBlank @Size(max = 5000) String description
) {
}