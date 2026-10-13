package smarttrade.backend.repository.auth;

import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import smarttrade.backend.entities.auth.WebSocketTicketEntity;

@Repository
public interface WebSocketTicketRepository
        extends JpaRepository<WebSocketTicketEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from WebSocketTicketEntity t where t.ticketHash = :ticketHash")
    Optional<WebSocketTicketEntity> findByTicketHashForUpdate(
            @Param("ticketHash") String ticketHash
    );

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from WebSocketTicketEntity t "
            + "where t.expiresAt <= :now or t.consumedAt is not null")
    int purgeExpiredOrConsumed(@Param("now") Instant now);
}