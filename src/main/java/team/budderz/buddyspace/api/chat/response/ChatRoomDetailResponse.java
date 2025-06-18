package team.budderz.buddyspace.api.chat.response;

import team.budderz.buddyspace.infra.database.chat.entity.ChatRoomType;

import java.time.LocalDateTime;

public record ChatRoomDetailResponse(
        String roomId,
        String name,
        String description,
        ChatRoomType type,
        Long createdBy,
        LocalDateTime createdAt,
        int participantCount
) {}
