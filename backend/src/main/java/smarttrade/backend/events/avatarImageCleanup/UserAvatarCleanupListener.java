package smarttrade.backend.events.avatarImageCleanup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import smarttrade.backend.service.cloudinary.UserAvatarCleanupService;

@Component
@RequiredArgsConstructor
public class UserAvatarCleanupListener {

    private final UserAvatarCleanupService cleanupService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserAvatarCleanupRequestedEvent event) {
        cleanupService.cleanupAsync(event.avatarIds());
    }
}