package smarttrade.backend.events.itemImageCleanup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import smarttrade.backend.service.cloudinary.ItemImageCleanupService;

@Component
@RequiredArgsConstructor
public class ItemImageCleanupListener {

    private final ItemImageCleanupService cleanupService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ItemImageCleanupRequestedEvent event) {
        cleanupService.cleanupAsync(event.imageIds());
    }
}