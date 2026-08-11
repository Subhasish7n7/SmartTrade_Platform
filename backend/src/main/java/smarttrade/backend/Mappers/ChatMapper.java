package smarttrade.backend.Mappers;

import org.springframework.stereotype.Component;
import smarttrade.backend.dto.chat.ChatConversationResponse;
import smarttrade.backend.dto.chat.ChatMessageResponse;
import smarttrade.backend.entities.chat.ChatConversationEntity;
import smarttrade.backend.entities.chat.ChatMessageEntity;
import smarttrade.backend.entities.UserEntity;

@Component
public class ChatMapper {

    public ChatMessageResponse toMessageResponse(ChatMessageEntity message) {

        return ChatMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getUserId())
                .senderName(message.getSender().getName())
                .message(message.getMessage())
                .timestamp(message.getCreatedAt())
                .build();

    }

    public ChatConversationResponse toConversationResponse(
            ChatConversationEntity conversation,
            UserEntity currentUser,
            ChatMessageEntity lastMessage,
            Integer activeTradeCount,
            Integer unreadCount
    ) {

        UserEntity otherUser = conversation.getUser1().getUserId().equals(currentUser.getUserId())
                                ?
                                conversation.getUser2()
                                :
                                conversation.getUser1();

        return ChatConversationResponse.builder()
                .conversationId(conversation.getId())
                .otherUserId(otherUser.getUserId())
                .otherUserName(otherUser.getName())
                .avatarUrl(otherUser.getAvatarUrl())
                .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt(): null)
                .unreadCount(unreadCount)
                .activeTradeCount(activeTradeCount)
                .pinnedTradeId(
                        conversation.getPinnedTrade() != null
                                ? conversation.getPinnedTrade().getTradeId()
                                : null
                )
                .build();

    }

}