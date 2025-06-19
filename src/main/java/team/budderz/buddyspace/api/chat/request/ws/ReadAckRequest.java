package team.budderz.buddyspace.api.chat.request.ws;

/* 클라이언트가 STOMP 로 보낼때 */
public record ReadAckRequest(
        Long userId,
        Long lastReadMessageId
) {}
