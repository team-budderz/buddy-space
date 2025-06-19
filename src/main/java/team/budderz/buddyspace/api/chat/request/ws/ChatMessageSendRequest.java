package team.budderz.buddyspace.api.chat.request.ws;

public record ChatMessageSendRequest(
        Long roomId,           // 채팅방 ID
        Long senderId,         // 보낸 사람 (userId)
        String messageType,    // 메시지 타입 (TEXT, IMAGE 등)
        String content,        // 메시지 내용
        String attachmentUrl   // 첨부파일 URL (선택)
) {
}
