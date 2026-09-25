package smarttrade.backend.entities.item;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "item_images",
        indexes = {
                @Index(name = "idx_item_images_item_status_order", columnList = "item_id,status,sort_order"),
                @Index(name = "idx_item_images_cleanup", columnList = "status,marked_for_deletion_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_item_images_asset_id", columnNames = "asset_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private ItemEntity item;

    @Column(name = "secure_url", nullable = false, columnDefinition = "TEXT")
    private String secureUrl;

    @Column(name = "public_id", columnDefinition = "TEXT")
    private String publicId;

    @Column(name = "asset_id", columnDefinition = "TEXT")
    private String assetId;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "format")
    private String format;

    @Column(name = "bytes")
    private Long bytes;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ItemImageStatus status = ItemImageStatus.ACTIVE;

    @Column(name = "marked_for_deletion_at")
    private LocalDateTime markedForDeletionAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}