package team.budderz.buddyspace.infra.database.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.budderz.buddyspace.infra.database.chat.entity.ChatMessage;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;

/**
 * {@link ChatMessage} 엔티티에 대한 JPA Repository 인터페이스입니다.
 * - 메시지 페이징 조회
 * - 읽지 않은 메시지 개수 계산
 * - 최신 메시지 조회 등을 제공합니다.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 해당 채팅방에서 가장 최근에 보낸 메시지를 조회합니다.
     *
     * @param chatRoom 채팅방 엔티티
     * @return 가장 최근 메시지 (없으면 null)
     */
    ChatMessage findTopByChatRoomOrderBySentAtDesc(ChatRoom chatRoom);

    /**
     * 사용자의 마지막 읽음 메시지 ID 이후에 작성된 메시지 개수를 조회합니다.
     * (읽지 않은 메시지 수 계산용)
     *
     * @param chatRoom 채팅방
     * @param lastReadMessageId 사용자의 마지막 읽음 메시지 ID
     * @return 읽지 않은 메시지 수
     */
    long countByChatRoomAndIdGreaterThan(ChatRoom chatRoom, Long lastReadMessageId);

    /**
     * 채팅방 메시지를 페이지 단위로 조회합니다. (최신순 정렬 가능)
     *
     * @param chatRoomId 채팅방 ID
     * @param pageable 페이징 정보
     * @return 페이징된 메시지 목록
     */
    Page<ChatMessage> findByChatRoom_Id(Long chatRoomId, Pageable pageable);

    /**
     * 특정 메시지 ID보다 큰 메시지 개수를 조회합니다.
     * - 읽지 않은 메시지 개수를 계산할 때 사용됩니다.
     *
     * @param roomId 채팅방 ID
     * @param lastReadMessageId 마지막 읽은 메시지 ID
     * @return 해당 조건에 부합하는 메시지 개수
     */
    long countByChatRoom_IdAndIdGreaterThan(Long roomId, Long lastReadMessageId);
}
