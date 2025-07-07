package team.budderz.buddyspace.infra.database.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;

import java.util.List;

/**
 * {@link ChatRoom} 엔티티에 대한 JPA Repository입니다.
 * - 채팅방 생성, 조회, 삭제 및 참여자 기반 검색 등의 기능을 제공합니다.
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 지정된 사용자 목록이 정확히 참여하고 있는 1:1(DIRECT) 채팅방을 조회합니다.
     * <p>
     * 조건:
     * - chatRoomType이 'DIRECT'
     * - 해당 방의 참여자가 지정한 participantIds와 정확히 일치
     *
     * @param participantIds 참여자 ID 목록
     * @param size 참여자 수 (participantIds의 크기와 동일해야 함)
     * @return 조건에 맞는 DIRECT 채팅방 리스트
     */
    @Query("""
        SELECT cr FROM ChatRoom cr
        WHERE cr.chatRoomType = 'DIRECT'
        AND cr.id IN (
            SELECT cp.chatRoom.id FROM ChatParticipant cp
            WHERE cp.user.id IN :participantIds
            GROUP BY cp.chatRoom.id
            HAVING COUNT(cp.user.id) = :size
        )
        """)
    List<ChatRoom> findDirectRoomByParticipants(@Param("participantIds") List<Long> participantIds,
                                                @Param("size") long size);

    /**
     * 특정 그룹에 속한 모든 채팅방을 삭제합니다.
     * (하위 메시지 및 참여자 정보는 cascade 설정으로 함께 삭제됨)
     *
     * @param groupId 그룹 ID
     */
    void deleteAllByGroup_Id(Long groupId);

    /**
     * 채팅방 내 현재 활성화된 참가자 수를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @return 활성화된 참가자 수
     */
    @Query("select count(p) from ChatParticipant p where p.chatRoom.id = :roomId and p.isActive = true")
    int countActiveParticipants(@Param("roomId") Long roomId);

    @Query("SELECT cr.group.id FROM ChatRoom cr WHERE cr.id = :roomId")
    Long findGroupIdByRoomId(@Param("roomId") Long roomId);

}
