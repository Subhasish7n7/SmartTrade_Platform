package smarttrade.backend.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trade_offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeOfferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UserEntity sender;

    @ManyToOne
    private UserEntity receiver;

    @ManyToMany
    @JoinTable(name = "trade_sender_items",
            joinColumns = @JoinColumn(name = "trade_offer_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<itemEntity> senderItems;

    @ManyToMany
    @JoinTable(name = "trade_receiver_items",
            joinColumns = @JoinColumn(name = "trade_offer_id"),
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<itemEntity> receiverItems;

    @ManyToOne
    @JoinColumn(name = "trade_id")
    private TradeEntity trade;

    @ManyToOne
    private UserEntity createdBy;

    private Double cashAdjustment;

    private LocalDateTime createdAt;
}

