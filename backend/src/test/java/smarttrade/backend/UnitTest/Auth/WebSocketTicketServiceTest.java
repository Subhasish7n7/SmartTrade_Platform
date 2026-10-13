package smarttrade.backend.UnitTest.Auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import smarttrade.backend.entities.auth.WebSocketTicketEntity;
import smarttrade.backend.entities.user.AccountStatus;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.repository.auth.WebSocketTicketRepository;
import smarttrade.backend.repository.user.UserRepo;
import smarttrade.backend.security.CustomUserDetailsService;
import smarttrade.backend.security.WebSocketTicketService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketTicketServiceTest {

    @Mock
    private WebSocketTicketRepository ticketRepository;

    @Mock
    private UserRepo userRepo;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private WebSocketTicketService ticketService;

    @Test
    void shouldIssueRandomShortLivedTicketAndPersistOnlyItsHash() {
        when(ticketRepository.save(any(WebSocketTicketEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var before = Instant.now();
        var response = ticketService.issueTicket("user@test.com");
        var after = Instant.now();

        ArgumentCaptor<WebSocketTicketEntity> captor =
                ArgumentCaptor.forClass(WebSocketTicketEntity.class);
        verify(ticketRepository).save(captor.capture());

        WebSocketTicketEntity saved = captor.getValue();
        assertEquals("user@test.com", saved.getUserEmail());
        assertEquals(64, saved.getTicketHash().length());
        assertEquals(30, response.expiresInSeconds());
        assertFalse(response.ticket().isBlank());
        assertNotEquals(response.ticket(), saved.getTicketHash());
        assertTrue(!saved.getExpiresAt().isBefore(before.plusSeconds(30))
                && !saved.getExpiresAt().isAfter(after.plusSeconds(30)));
    }

    @Test
    void shouldConsumeTicketOnceAndReturnAuthenticatedPrincipal() throws Exception {
        String rawTicket = "ticket-for-test";
        String hash = sha256(rawTicket);
        WebSocketTicketEntity ticket = new WebSocketTicketEntity(
                hash, "user@test.com", Instant.now().plusSeconds(30)
        );
        UserEntity user = mock(UserEntity.class);
        when(user.getEmail()).thenReturn("user@test.com");
        when(user.getAccountStatus()).thenReturn(AccountStatus.ACTIVE);
        when(ticketRepository.findByTicketHashForUpdate(hash)).thenReturn(Optional.of(ticket));
        when(userRepo.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        UserDetails details = mock(UserDetails.class);
        when(details.isEnabled()).thenReturn(true);
        when(details.isAccountNonLocked()).thenReturn(true);
        when(details.isAccountNonExpired()).thenReturn(true);
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(details).getAuthorities();
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(details);

        Authentication authentication = ticketService.consumeAndAuthenticate(rawTicket);

        assertSame(details, authentication.getPrincipal());
        assertNotNull(ticket.getConsumedAt());
        verify(ticketRepository).saveAndFlush(ticket);
        assertThrows(BadCredentialsException.class,
                () -> ticketService.consumeAndAuthenticate(rawTicket));
    }

    @Test
    void shouldRejectUnknownTicket() throws Exception {
        String rawTicket = "unknown-ticket";
        when(ticketRepository.findByTicketHashForUpdate(sha256(rawTicket)))
                .thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                () -> ticketService.consumeAndAuthenticate(rawTicket));
    }

    @Test
    void shouldRejectExpiredTicket() throws Exception {
        String rawTicket = "expired-ticket";
        WebSocketTicketEntity ticket = new WebSocketTicketEntity(
                sha256(rawTicket), "user@test.com", Instant.now().minusSeconds(1)
        );
        when(ticketRepository.findByTicketHashForUpdate(sha256(rawTicket)))
                .thenReturn(Optional.of(ticket));

        assertThrows(BadCredentialsException.class,
                () -> ticketService.consumeAndAuthenticate(rawTicket));
        verifyNoInteractions(userRepo, userDetailsService);
    }

    private static String sha256(String value) throws NoSuchAlgorithmException {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                        .digest(value.getBytes(StandardCharsets.UTF_8))
        );
    }
}