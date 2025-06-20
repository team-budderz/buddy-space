package team.budderz.buddyspace.infra.database.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.chat.entity.ChatMessage;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 채팅방 목록 화면에 최근 메시지 내용 (해당 채팅방에서 가장 최근에 보낸 메시지 1개 추출)
    ChatMessage findTopByChatRoomOrderBySentAtDesc(ChatRoom chatRoom);

    // 안 읽은 메시지 수
    long countByChatRoomAndIdGreaterThan(ChatRoom chatRoom, Long lastReadMessageId);

    // 페이징 처리된 채팅방 메시지 조회
    Page<ChatMessage> findByChatRoom_Id(Long chatRoomId, Pageable pageable);

    // 특정 메시지 ID 보다 큰, 해당 방의 메시지 개수 (unreadCount 계산용)
    long countByChatRoom_IdAndIdGreaterThan(Long roomId, Long lastReadMessageId);

}
