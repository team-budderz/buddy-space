package team.budderz.buddyspace.api.chat.response.rest;

import java.time.LocalDateTime;

/**
 * 채팅방 수정 응답 DTO
 */
public record UpdateChatRoomResponse(
        String roomId,
        String name,
        String description,
        LocalDateTime modifiedAt
) {}
