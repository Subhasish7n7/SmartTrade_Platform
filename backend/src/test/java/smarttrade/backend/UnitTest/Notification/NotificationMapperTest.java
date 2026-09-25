package smarttrade.backend.UnitTest.Notification;

import org.junit.jupiter.api.Test;
import smarttrade.backend.Mappers.NotificationMapper;
import smarttrade.backend.dto.notification.NotificationResponse;
import smarttrade.backend.entities.user.UserEntity;
import smarttrade.backend.entities.notification.NotificationEntity;
import smarttrade.backend.entities.notification.NotificationResourceType;
import smarttrade.backend.entities.notification.NotificationType;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationMapperTest {

    private final NotificationMapper notificationMapper =
            new NotificationMapper();

    @Test
    void shouldMapNotificationWithActor() {
        UserEntity actor = UserEntity.builder()
                .userId(1L)
                .name("Actor")
                .build();

        LocalDateTime createdAt = LocalDateTime.now().minusMinutes(5);
        LocalDateTime readAt = LocalDateTime.now();

        NotificationEntity entity = NotificationEntity.builder()
                .id(10L)
                .actor(actor)
                .type(NotificationType.TRADE_ACCEPTED)
                .resourceType(NotificationResourceType.TRADE)
                .resourceId(50L)
                .message("Actor accepted your trade.")
                .isRead(true)
                .createdAt(createdAt)
                .readAt(readAt)
                .build();

        NotificationResponse result = notificationMapper.map(entity);

        assertEquals(10L, result.getId());
        assertEquals("TRADE_ACCEPTED", result.getType());
        assertEquals("TRADE", result.getResourceType());
        assertEquals(50L, result.getResourceId());
        assertEquals(1L, result.getActorId());
        assertEquals("Actor", result.getActorName());
        assertEquals("Actor accepted your trade.", result.getMessage());
        assertTrue(result.isRead());
        assertEquals(createdAt, result.getCreatedAt());
        assertEquals(readAt, result.getReadAt());
    }

    @Test
    void shouldMapNotificationWithoutActor() {
        NotificationEntity entity = NotificationEntity.builder()
                .id(20L)
                .actor(null)
                .type(NotificationType.SYSTEM)
                .resourceType(NotificationResourceType.SYSTEM)
                .resourceId(2L)
                .message("System maintenance tonight.")
                .isRead(false)
                .build();

        NotificationResponse result = notificationMapper.map(entity);

        assertEquals(20L, result.getId());
        assertEquals("SYSTEM", result.getType());
        assertEquals("SYSTEM", result.getResourceType());
        assertEquals(2L, result.getResourceId());
        assertNull(result.getActorId());
        assertNull(result.getActorName());
        assertEquals("System maintenance tonight.", result.getMessage());
        assertFalse(result.isRead());
        assertNull(result.getCreatedAt());
        assertNull(result.getReadAt());
    }
}
