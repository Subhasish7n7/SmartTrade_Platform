package smarttrade.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.dto.chat.ChatInboxResponse;
import smarttrade.backend.dto.chat.ChatMessageRequest;
import smarttrade.backend.dto.chat.ChatMessageResponse;
import smarttrade.backend.dto.chat.PinTradeRequest;
import smarttrade.backend.entities.UserEntity;
import smarttrade.backend.service.ChatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;


    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageRequest request) {

        chatService.sendMessage(request.getOtherUserId(), request.getMessage()
        );
    }
    @PostMapping("/chat/{conversationId}/active")
    public void enterConversation(
            @PathVariable Long conversationId) {

        chatService.enterConversation(conversationId);
    }

    @DeleteMapping("/chat/active")
    public void leaveConversation() {

        chatService.leaveConversation();
    }

    @GetMapping("/chat/{otherUserId}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable Long otherUserId) {
        return chatService.getConversation(otherUserId);
    }
    @GetMapping("/chat/inbox")
    public List<ChatInboxResponse> getInbox() {

        return chatService.getInbox();
    }
    @PutMapping("/chat/{otherUserId}/pin")
    public void pinTrade(@PathVariable Long otherUserId, @RequestBody PinTradeRequest request) {

        chatService.pinTrade(otherUserId, request.getTradeId());
    }
}