package smarttrade.backend.events.trust;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import smarttrade.backend.events.trust.TrustEvent;
import smarttrade.backend.service.trust.TrustScoreService;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrustEventListener {

    private final TrustScoreService trustScoreService;

    @EventListener
    public void handle(TrustEvent event) {

        log.debug(
                "Processing trust event: type={}, userId={}, sourceType={}, sourceId={}",
                event.type(),
                event.userId(),
                event.sourceType(),
                event.sourceId()
        );

        trustScoreService.process(event);
    }
}