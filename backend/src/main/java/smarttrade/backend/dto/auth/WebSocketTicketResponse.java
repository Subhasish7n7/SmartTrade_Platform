package smarttrade.backend.dto.auth;

public record WebSocketTicketResponse(String ticket, long expiresInSeconds) {
}