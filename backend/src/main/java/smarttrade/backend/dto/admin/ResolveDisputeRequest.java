package smarttrade.backend.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import smarttrade.backend.entities.trade.DisputeResolution;

public record ResolveDisputeRequest(
        @NotNull DisputeResolution resolution,
        Long faultUserId,
        @Size(max = 5000) String notes
) {
}