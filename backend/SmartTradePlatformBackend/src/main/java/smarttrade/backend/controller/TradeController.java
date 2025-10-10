package smarttrade.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.Mappers.TradeMapper;
import smarttrade.backend.dto.TradeOfferDto;
import smarttrade.backend.entities.TradeOfferEntity;
import smarttrade.backend.service.TradeService;

@RestController
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final TradeMapper tradeMapper;

    @PostMapping("/offer")
    public ResponseEntity<TradeOfferDto> createTrade(@RequestBody TradeOfferDto dto) {
        TradeOfferEntity tradeOffer= tradeMapper.mapToEntity(dto);
        TradeOfferDto tradeDto=tradeMapper.mapFromEntity(tradeService.createTradeOffer(tradeOffer));
        return ResponseEntity.status(HttpStatus.CREATED).body(tradeDto);
    }

    @PatchMapping("/{tradeId}/status")
    public ResponseEntity<Void> updateTradeStatus(@PathVariable Long tradeId,
                                                  @RequestParam String status) {
        tradeService.updateTradeStatus(tradeId, status);
        return ResponseEntity.ok().build();
    }
}


