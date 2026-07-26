package smarttrade.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.dto.notification.NotificationResponse;
import smarttrade.backend.service.NotificationService;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Page<NotificationResponse> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){

        return notificationService.getNotifications(page,size);
    }
    @DeleteMapping("/{id}")
    public void deleteNotification(
            @PathVariable Long id) {

        notificationService.delete(id);
    }

    @GetMapping("/unread-count")
    public Long unreadCount(){

        return notificationService.getUnreadCount();
    }

    @PatchMapping("/{id}/read")
    public void markRead(@PathVariable Long id){

        notificationService.markRead(id);
    }

    @PatchMapping("/read-all")
    public void markAllRead(){

        notificationService.markAllRead();
    }
}