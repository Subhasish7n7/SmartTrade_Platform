package smarttrade.backend.UnitTest.Chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import smarttrade.backend.controller.ChatController;
import smarttrade.backend.dto.chat.ChatConversationResponse;
import smarttrade.backend.dto.chat.ChatMessageRequest;
import smarttrade.backend.dto.chat.ChatMessageResponse;
import smarttrade.backend.dto.chat.ConversationPresenceRequest;
import smarttrade.backend.service.ChatService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private ChatController chatController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
    }

    @Test
    void shouldSendMessageThroughWebSocketController() {
        ChatMessageRequest request = new ChatMessageRequest();
        request.setOtherUserId(2L);
        request.setMessage("Hello");

        chatController.sendMessage(request);

        verify(chatService).sendMessage(2L, "Hello");
    }

    @Test
    void shouldUpdatePresenceThroughWebSocketController() {
        ConversationPresenceRequest request = new ConversationPresenceRequest();
        request.setConversationId(10L);

        when(headerAccessor.getSessionId()).thenReturn("session-1");

        chatController.updatePresence(request, headerAccessor);

        verify(chatService).updatePresence("session-1", 10L);
    }

    @Test
    void shouldGetConversationMessagesWithDefaultPageAndSize() throws Exception {
        ChatMessageResponse response = ChatMessageResponse.builder()
                .id(100L).conversationId(10L).senderId(1L)
                .senderName("Sender").message("Hello").build();

        when(chatService.getConversation(2L, 0, 40))
                .thenReturn(new PageImpl<>(
                        List.of(response), PageRequest.of(0, 40), 1
                ));

        mockMvc.perform(get("/chat/2/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(100))
                .andExpect(jsonPath("$.content[0].conversationId").value(10))
                .andExpect(jsonPath("$.content[0].message").value("Hello"));

        verify(chatService).getConversation(2L, 0, 40);
    }

    @Test
    void shouldGetConversationMessagesWithRequestedPageAndSize() throws Exception {
        when(chatService.getConversation(2L, 2, 25))
                .thenReturn(new PageImpl<>(
                        List.of(), PageRequest.of(2, 25), 0
                ));

        mockMvc.perform(
                get("/chat/2/messages")
                        .param("page", "2")
                        .param("size", "25")
        ).andExpect(status().isOk());

        verify(chatService).getConversation(2L, 2, 25);
    }

    @Test
    void shouldGetInbox() throws Exception {
        ChatConversationResponse response =
                ChatConversationResponse.builder()
                        .conversationId(10L).otherUserId(2L)
                        .otherUserName("Other").lastMessage("Hello")
                        .unreadCount(2).activeTradeCount(1).build();

        when(chatService.getInbox(null)).thenReturn(List.of(response));

        mockMvc.perform(get("/chat/inbox"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conversationId").value(10))
                .andExpect(jsonPath("$[0].otherUserId").value(2))
                .andExpect(jsonPath("$[0].otherUserName").value("Other"))
                .andExpect(jsonPath("$[0].lastMessage").value("Hello"))
                .andExpect(jsonPath("$[0].unreadCount").value(2))
                .andExpect(jsonPath("$[0].activeTradeCount").value(1));

        verify(chatService).getInbox(null);
    }

    @Test
    void shouldPinTrade() throws Exception {
        mockMvc.perform(
                put("/chat/2/pin")
                        .contentType("application/json")
                        .content("{\"tradeId\":50}")
        ).andExpect(status().isOk());

        verify(chatService).pinTrade(2L, 50L);
    }
}
