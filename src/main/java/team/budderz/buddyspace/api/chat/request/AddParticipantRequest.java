package team.budderz.buddyspace.api.chat.request;

/**
 * 채팅방에 새 사용자 초대 요청 DTO
 */
public record AddParticipantRequest(
        Long userId
) {}
