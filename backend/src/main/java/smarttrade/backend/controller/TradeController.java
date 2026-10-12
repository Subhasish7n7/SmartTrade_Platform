package smarttrade.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.Mappers.TradeMapper;
import smarttrade.backend.dto.trade.*;
import smarttrade.backend.entities.trade.TradeOfferEntity;
import smarttrade.backend.service.trade.TradeDisputeService;
import smarttrade.backend.service.trade.TradeService;

import java.util.List;

@RestController
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final TradeMapper tradeMapper;
    private final TradeDisputeService tradeDisputeService;

    @PostMapping("/offer")
    public ResponseEntity<TradeOfferResponse> createTrade(@RequestBody CreateTradeOfferRequest request) {
        System.out.println("end point hit " + request);
        TradeOfferEntity tradeOffer = tradeService.createTradeOffer(request);
        TradeOfferResponse response = tradeMapper.mapFromEntity(tradeOffer);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/buy")
    public ResponseEntity<TradeOfferResponse> createBuyOffer(@RequestBody CreateBuyRequest request) {

        TradeOfferEntity tradeOffer = tradeService.createBuyOffer(request);

        TradeOfferResponse response = tradeMapper.mapFromEntity(tradeOffer);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<TradeInboxResponse>> getTradeInbox() {
        return ResponseEntity.ok(tradeService.getTradeInbox());
    }

    @GetMapping("/{tradeId}")
    public ResponseEntity<TradeDetailsResponse> getTrade(@PathVariable Long tradeId,
                                                         @RequestParam Long tradeOfferId) {
        return ResponseEntity.ok(tradeService.getTrade(tradeId, tradeOfferId));
    }

    @GetMapping("/users/{userId}/tradeable-items")
    public ResponseEntity<List<TradeItemResponse>> getTradeableItems(@PathVariable Long userId) {
        return ResponseEntity.ok(tradeService.getTradeableItems(userId));
    }

    @GetMapping("/{tradeId}/offers")
    public ResponseEntity<List<TradeOfferResponse>> getTradeOfferHistory(@PathVariable Long tradeId) {
        return ResponseEntity.ok(tradeService.getTradeOfferHistory(tradeId));
    }

    @PatchMapping("/{tradeId}/accept")
    public ResponseEntity<Void> acceptTrade(@PathVariable Long tradeId) {
        tradeService.acceptTrade(tradeId);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/{tradeId}/confirm-completion")
    public ResponseEntity<Void> confirmCompletion(
            @PathVariable Long tradeId) {

        tradeService.confirmCompletion(tradeId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{tradeId}/dispute")
    public ResponseEntity<TradeDisputeResponse> openDispute(
            @PathVariable Long tradeId,
            @Valid @RequestBody CreateDisputeRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tradeDisputeService.openDispute(tradeId, request));
    }

    @GetMapping("/{tradeId}/dispute")
    public ResponseEntity<TradeDisputeResponse> getDispute(@PathVariable Long tradeId) {
        return ResponseEntity.ok(tradeDisputeService.getUserDispute(tradeId));
    }

    @PatchMapping("/{tradeId}/cancel")
    public ResponseEntity<Void> cancelTrade(@PathVariable Long tradeId) {
        tradeService.cancelTrade(tradeId);
        return ResponseEntity.ok().build();
    }

}


