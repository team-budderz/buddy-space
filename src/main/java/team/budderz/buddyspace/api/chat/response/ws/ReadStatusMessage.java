package team.budderz.buddyspace.api.chat.response.ws;

import java.util.List;

/*
STOMP 메시지로 주고받을 별도의 페이로드 타입
: outgoing (서버 → 구독 클라이언트): 최신 읽음 상태를 브로드캐스트할 때 쓰는 메시지
 */
public record ReadStatusMessage(
        Long roomId,
        List<Participant> participants
) {
    public record Participant(Long userId, Long lastReadMessageId) {}
}
