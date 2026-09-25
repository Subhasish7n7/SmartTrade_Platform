package smarttrade.backend.UnitTest.Chat;

import org.junit.jupiter.api.Test;
import smarttrade.backend.Mappers.ChatMapper;
import smarttrade.backend.TestDataUtil.userTestData;
import smarttrade.backend.dto.chat.ChatConversationResponse;
import smarttrade.backend.dto.chat.ChatMessageResponse;
import smarttrade.backend.entities.chat.ChatConversationEntity;
import smarttrade.backend.entities.chat.ChatMessageEntity;
import smarttrade.backend.entities.trade.TradeEntity;
import smarttrade.backend.entities.user.UserEntity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ChatMapperTest {

    private final ChatMapper chatMapper = new ChatMapper();

    @Test
    void shouldMapMessageResponse() {
        UserEntity sender = userTestData.CreateUserA();
        sender.setUserId(1L);

        UserEntity receiver = userTestData.CreateUserB();
        receiver.setUserId(2L);

        ChatConversationEntity conversation = ChatConversationEntity.builder()
                .id(10L)
                .user1(sender)
                .user2(receiver)
                .build();

        LocalDateTime createdAt = LocalDateTime.now();

        ChatMessageEntity message = ChatMessageEntity.builder()
                .id(100L)
                .sender(sender)
                .conversation(conversation)
                .message("Hello")
                .createdAt(createdAt)
                .build();

        ChatMessageResponse result =
                chatMapper.toMessageResponse(message);

        assertEquals(100L, result.getId());
        assertEquals(10L, result.getConversationId());
        assertEquals(1L, result.getSenderId());
        assertEquals(sender.getName(), result.getSenderName());
        assertEquals("Hello", result.getMessage());
        assertEquals(createdAt, result.getTimestamp());
    }

    @Test
    void shouldMapConversationFromUser1Perspective() {
        UserEntity user1 = userTestData.CreateUserA();
        user1.setUserId(1L);

        UserEntity user2 = userTestData.CreateUserB();
        user2.setUserId(2L);

        TradeEntity trade = TradeEntity.builder()
                .tradeId(50L)
                .build();

        ChatConversationEntity conversation =
                ChatConversationEntity.builder()
                        .id(10L)
                        .user1(user1)
                        .user2(user2)
                        .pinnedTrade(trade)
                        .build();

        LocalDateTime timestamp = LocalDateTime.now();

        ChatMessageEntity lastMessage =
                ChatMessageEntity.builder()
                        .id(100L)
                        .conversation(conversation)
                        .sender(user2)
                        .message("Latest message")
                        .createdAt(timestamp)
                        .build();

        ChatConversationResponse result =
                chatMapper.toConversationResponse(
                        conversation,
                        user1,
                        lastMessage,
                        3,
                        5
                );

        assertEquals(10L, result.getConversationId());
        assertEquals(2L, result.getOtherUserId());
        assertEquals(user2.getName(), result.getOtherUserName());
        assertEquals(user2.getAvatar().getSecureUrl(), result.getAvatarUrl());
        assertEquals("Latest message", result.getLastMessage());
        assertEquals(timestamp, result.getLastMessageAt());
        assertEquals(5, result.getUnreadCount());
        assertEquals(3, result.getActiveTradeCount());
        assertEquals(50L, result.getPinnedTradeId());
    }

    @Test
    void shouldMapConversationFromUser2Perspective() {
        UserEntity user1 = userTestData.CreateUserA();
        user1.setUserId(1L);

        UserEntity user2 = userTestData.CreateUserB();
        user2.setUserId(2L);

        ChatConversationEntity conversation =
                ChatConversationEntity.builder()
                        .id(10L)
                        .user1(user1)
                        .user2(user2)
                        .build();

        ChatConversationResponse result =
                chatMapper.toConversationResponse(
                        conversation,
                        user2,
                        null,
                        0,
                        2
                );

        assertEquals(1L, result.getOtherUserId());
        assertEquals(user1.getName(), result.getOtherUserName());
        assertEquals(user1.getAvatar().getSecureUrl(), result.getAvatarUrl());
        assertNull(result.getLastMessage());
        assertNull(result.getLastMessageAt());
        assertEquals(2, result.getUnreadCount());
        assertEquals(0, result.getActiveTradeCount());
        assertNull(result.getPinnedTradeId());
    }
}