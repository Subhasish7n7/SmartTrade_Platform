package smarttrade.backend.entities.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String name;
    private String phone_no;

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
    private UserAvatarEntity avatar;

    @Builder.Default
    @Column(name = "trust_score", nullable = false)
    private double trustScore = 500.0;
    @Builder.Default
    @Column(name = "total_listings", nullable = false)
    private int totalListings = 0;
    @Builder.Default
    @Column(name = "successful_trades", nullable = false)
    private int successfulTrades = 0;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @PrePersist
    private void prePersist() {
        if (role == null) role = UserRole.USER;
        if (accountStatus == null) accountStatus = AccountStatus.ACTIVE;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
