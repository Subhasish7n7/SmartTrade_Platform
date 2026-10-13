package smarttrade.backend.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smarttrade.backend.dto.auth.WebSocketTicketResponse;
import smarttrade.backend.entities.auth.WebSocketTicketEntity;
import smarttrade.backend.entities.user.AccountStatus;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.auth.WebSocketTicketRepository;
import smarttrade.backend.repository.user.UserRepo;

@Service
public class WebSocketTicketService {

    private static final Duration TICKET_TTL = Duration.ofSeconds(30);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final WebSocketTicketRepository ticketRepository;
    private final UserRepo userRepo;
    private final CustomUserDetailsService userDetailsService;

    public WebSocketTicketService(
            WebSocketTicketRepository ticketRepository,
            UserRepo userRepo,
            CustomUserDetailsService userDetailsService
    ) {
        this.ticketRepository = ticketRepository;
        this.userRepo = userRepo;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public WebSocketTicketResponse issueTicket(String email) {
        Instant now = Instant.now();
        ticketRepository.purgeExpiredOrConsumed(now);

        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String rawTicket = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        ticketRepository.save(new WebSocketTicketEntity(
                sha256(rawTicket), email, now.plus(TICKET_TTL)
        ));

        return new WebSocketTicketResponse(rawTicket, TICKET_TTL.toSeconds());
    }

    @Transactional
    public Authentication consumeAndAuthenticate(String rawTicket) {
        if (rawTicket == null || rawTicket.isBlank()) {
            throw invalidTicket();
        }

        Instant now = Instant.now();
        String ticketHash = sha256(rawTicket);

        var ticket = ticketRepository.findByTicketHashForUpdate(ticketHash)
                .orElseThrow(this::invalidTicket);

        if (ticket.getConsumedAt() != null || !ticket.getExpiresAt().isAfter(now)) {
            throw invalidTicket();
        }

        UserEntity user = userRepo.findByEmail(ticket.getUserEmail())
                .orElseThrow(this::invalidTicket);

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw invalidTicket();
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()
                || !userDetails.isAccountNonExpired()) {
            throw invalidTicket();
        }

        // The pessimistic row lock makes this consume operation single-use across
        // application instances. Commit consumedAt before CONNECT is accepted.
        ticket.setConsumedAt(now);
        ticketRepository.saveAndFlush(ticket);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private BadCredentialsException invalidTicket() {
        return new BadCredentialsException("Invalid, expired, or already-used WebSocket ticket");
    }
}