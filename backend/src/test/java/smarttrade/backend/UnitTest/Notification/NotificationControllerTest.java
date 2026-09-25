package smarttrade.backend.UnitTest.Notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import smarttrade.backend.controller.NotificationController;
import smarttrade.backend.dto.notification.NotificationCountResponse;
import smarttrade.backend.dto.notification.NotificationResponse;
import smarttrade.backend.entities.notification.NotificationResourceType;
import smarttrade.backend.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(notificationController)
                .build();
    }

    @Test
    void shouldGetNotificationsWithDefaultPageAndSize() throws Exception {
        NotificationResponse notification =
                NotificationResponse.builder()
                        .id(10L)
                        .type("TRADE_ACCEPTED")
                        .resourceType("TRADE")
                        .resourceId(50L)
                        .actorId(1L)
                        .actorName("Actor")
                        .message("Actor accepted your trade.")
                        .read(false)
                        .createdAt(LocalDateTime.now())
                        .build();

        when(notificationService.getNotifications(0, 20))
                .thenReturn(new PageImpl<>(List.of(notification), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].type").value("TRADE_ACCEPTED"))
                .andExpect(jsonPath("$.content[0].resourceType").value("TRADE"));

        verify(notificationService).getNotifications(0, 20);
    }

    @Test
    void shouldGetNotificationsWithRequestedPageAndSize() throws Exception {
        when(notificationService.getNotifications(2, 50))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(2, 50), 0));

        mockMvc.perform(
                        get("/notifications")
                                .param("page", "2")
                                .param("size", "50")
                )
                .andExpect(status().isOk());

        verify(notificationService).getNotifications(2, 50);
    }

    @Test
    void shouldDeleteNotification() throws Exception {
        mockMvc.perform(delete("/notifications/10"))
                .andExpect(status().isOk());

        verify(notificationService).delete(10L);
    }

    @Test
    void shouldGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount()).thenReturn(7L);

        mockMvc.perform(get("/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));

        verify(notificationService).getUnreadCount();
    }

    @Test
    void shouldMarkNotificationRead() throws Exception {
        mockMvc.perform(patch("/notifications/10/read"))
                .andExpect(status().isOk());

        verify(notificationService).markRead(10L);
    }

    @Test
    void shouldMarkModuleRead() throws Exception {
        mockMvc.perform(
                        patch("/notifications/module-read")
                                .param(
                                        "resourceType",
                                        NotificationResourceType.TRADE.name()
                                )
                )
                .andExpect(status().isOk());

        verify(notificationService).markModuleRead(
                NotificationResourceType.TRADE
        );
    }

    @Test
    void shouldGetCounts() throws Exception {
        NotificationCountResponse counts =
                NotificationCountResponse.builder()
                        .unreadCount(7L)
                        .tradeAlertCount(4L)
                        .chatAlertCount(3L)
                        .build();

        when(notificationService.getNotificationCounts())
                .thenReturn(counts);

        mockMvc.perform(get("/notifications/counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(7))
                .andExpect(jsonPath("$.tradeAlertCount").value(4))
                .andExpect(jsonPath("$.chatAlertCount").value(3));

        verify(notificationService).getNotificationCounts();
    }

    @Test
    void shouldMarkAllRead() throws Exception {
        mockMvc.perform(patch("/notifications/read-all"))
                .andExpect(status().isOk());

        verify(notificationService).markAllRead();
    }
}
