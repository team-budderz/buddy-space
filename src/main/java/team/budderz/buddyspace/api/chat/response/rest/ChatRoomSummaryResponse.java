package team.budderz.buddyspace.api.chat.response.rest;

import java.time.LocalDateTime;

public record ChatRoomSummaryResponse(
        Long roomId,
        String name,
        String lastMessage,
        String lastMessageType,
        LocalDateTime lastMessageTime,
        long unreadCount
) {}
