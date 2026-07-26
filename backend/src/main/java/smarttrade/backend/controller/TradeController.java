package smarttrade.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.Mappers.TradeMapper;
import smarttrade.backend.dto.trade.*;
import smarttrade.backend.entities.TradeOfferEntity;
import smarttrade.backend.service.TradeService;

import java.util.List;

@RestController
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final TradeMapper tradeMapper;

    @PostMapping("/offer")
    public ResponseEntity<TradeOfferResponse> createTrade(@RequestBody CreateTradeOfferRequest request) {
        TradeOfferEntity tradeOffer = tradeService.createTradeOffer(request);
        TradeOfferResponse response = tradeMapper.mapFromEntity(tradeOffer);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/buy")
    public ResponseEntity<TradeOfferResponse> createBuyOffer(@RequestBody CreateBuyRequest request){

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
        return ResponseEntity.ok(tradeService.getTrade(tradeId,tradeOfferId));
    }
    @GetMapping("/users/{userId}/tradeable-items")
    public ResponseEntity<List<TradeItemResponse>> getTradeableItems(@PathVariable Long userId){
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

    @PatchMapping("/{tradeId}/request-completion")
    public ResponseEntity<Void> requestCompletion(
            @PathVariable Long tradeId) {

        tradeService.requestCompletion(tradeId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{tradeId}/confirm-completion")
    public ResponseEntity<Void> confirmCompletion(
            @PathVariable Long tradeId) {

        tradeService.confirmCompletion(tradeId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{tradeId}/cancel")
    public ResponseEntity<Void> cancelTrade(@PathVariable Long tradeId) {
        tradeService.cancelTrade(tradeId);
        return ResponseEntity.ok().build();
    }

}


