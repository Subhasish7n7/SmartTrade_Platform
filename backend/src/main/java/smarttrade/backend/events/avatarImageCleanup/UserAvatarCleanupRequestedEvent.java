package smarttrade.backend.events.avatarImageCleanup;

import java.util.List;

public record UserAvatarCleanupRequestedEvent(
        List<Long> avatarIds
) {
}