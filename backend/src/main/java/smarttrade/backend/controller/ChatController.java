package smarttrade.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;
import smarttrade.backend.dto.chat.*;
import smarttrade.backend.service.ChatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;


    @MessageMapping("/chat.send")
    public void sendMessage(@Valid ChatMessageRequest request) {

        chatService.sendMessage(request.getOtherUserId(), request.getMessage());
    }
    @MessageMapping("/chat.presence")
    public void updatePresence(ConversationPresenceRequest request, SimpMessageHeaderAccessor accessor) {

        chatService.updatePresence(accessor.getSessionId(), request.getConversationId());
    }

    @GetMapping("/chat/{otherUserId}/messages")
    public Page<ChatMessageResponse> getMessages(@PathVariable Long otherUserId,
                                                 @RequestParam(defaultValue="0") int page,
                                                 @RequestParam(defaultValue="40") int size
    ){

        return chatService.getConversation(otherUserId, page, size);
    }
    @GetMapping("/chat/inbox")
    public List<ChatConversationResponse> getInbox() {
        return chatService.getInbox();
    }

    @PutMapping("/chat/{otherUserId}/pin")
    public void pinTrade(@PathVariable Long otherUserId, @RequestBody PinTradeRequest request) {

        chatService.pinTrade(otherUserId, request.getTradeId());
    }
}