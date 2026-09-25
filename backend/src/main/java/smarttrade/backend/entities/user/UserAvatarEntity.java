package smarttrade.backend.entities.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_avatars",
        indexes = {
                @Index(
                        name = "idx_user_avatars_cleanup",
                        columnList = "status,marked_for_deletion_at"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_avatars_asset_id",
                        columnNames = "asset_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAvatarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private UserAvatarStatus status = UserAvatarStatus.ACTIVE;

    @Column(name = "marked_for_deletion_at")
    private LocalDateTime markedForDeletionAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}