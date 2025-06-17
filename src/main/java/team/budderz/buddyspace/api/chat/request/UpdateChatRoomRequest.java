package team.budderz.buddyspace.api.chat.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 채팅방 수정 요청 DTO
 */
public record UpdateChatRoomRequest(
        @NotBlank String name,
        @NotBlank String description
) {}
