package team.budderz.buddyspace.api.chat.response;

/**
 * 채팅방 생성 응답 DTO
 */
public record CreateChatRoomResponse(
        String roomId,   // 생성된 방 ID
        String name,     // 생성된 방 이름
        String status    // ex. success
) {}
