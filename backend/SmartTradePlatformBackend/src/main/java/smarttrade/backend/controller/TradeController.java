package smarttrade.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.Mappers.TradeMapper;
import smarttrade.backend.dto.CreateTradeOfferRequest;
import smarttrade.backend.dto.TradeOfferDto;
import smarttrade.backend.dto.TradeOfferResponse;
import smarttrade.backend.entities.TradeOfferEntity;
import smarttrade.backend.service.TradeService;

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

    @PatchMapping("/{tradeId}/accept")
    public ResponseEntity<Void> acceptTrade(@PathVariable Long tradeId) {
        tradeService.acceptTrade(tradeId);
        return ResponseEntity.ok().build();
    }
}


