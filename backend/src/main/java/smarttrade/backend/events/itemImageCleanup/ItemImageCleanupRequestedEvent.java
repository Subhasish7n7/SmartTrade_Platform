package smarttrade.backend.events.itemImageCleanup;

import java.util.List;

public record ItemImageCleanupRequestedEvent(
        List<Long> imageIds
) {
}