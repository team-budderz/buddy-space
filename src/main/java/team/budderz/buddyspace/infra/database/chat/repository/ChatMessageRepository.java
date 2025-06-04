package team.budderz.buddyspace.infra.database.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.chat.entity.ChatMessage;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 채팅방 목록 화면에 최근 메시지 내용 (해당 채팅방에서 가장 최근에 보낸 메시지 1개 추출)
    ChatMessage findTopByChatRoomOrderBySentAtDesc(ChatRoom chatRoom);

    // 안 읽은 메시지 수
    long countByChatRoomAndIdGreaterThan(ChatRoom chatRoom, Long lastReadMessageId);
}
