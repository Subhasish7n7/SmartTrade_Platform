package smarttrade.backend.events;

import java.util.List;

public record ItemImageCleanupRequestedEvent(
        List<Long> imageIds
) {
}