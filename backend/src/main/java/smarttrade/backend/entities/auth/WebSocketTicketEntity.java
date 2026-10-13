
package smarttrade.backend.entities.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "websocket_auth_ticket",
        indexes = @Index(
                name = "idx_websocket_auth_ticket_expires_at",
                columnList = "expires_at"
        )
)
public class WebSocketTicketEntity {

    @Id
    @Column(name = "ticket_hash", nullable = false, length = 64)
    private String ticketHash;

    @Column(name = "user_email", nullable = false, length = 320)
    private String userEmail;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Setter
    @Column(name = "consumed_at")
    private Instant consumedAt;

    protected WebSocketTicketEntity() {
    }

    public WebSocketTicketEntity(
            String ticketHash,
            String userEmail,
            Instant expiresAt
    ) {
        this.ticketHash = ticketHash;
        this.userEmail = userEmail;
        this.expiresAt = expiresAt;
    }
}
