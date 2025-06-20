package team.budderz.buddyspace.infra.database.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // DIRECT 채팅방(1:1 채팅방)이 이미 존재하는지 확인
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

    // 특정 그룹에 속한 모든 레코드 한 번에 삭제
    void deleteAllByGroup_Id(Long groupId);

    // 방별 활성 참가자 수 조회
    @Query("select count(p) from ChatParticipant p where p.chatRoom.id = :roomId and p.isActive = true")
    int countActiveParticipants(@Param("roomId") Long roomId);
}
