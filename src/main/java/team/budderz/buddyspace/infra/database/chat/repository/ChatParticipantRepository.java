package team.budderz.buddyspace.infra.database.chat.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipantId;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ChatParticipantId> {
    // 내가 어떤 채팅방에 참여중인지 조회
    @Query("""
    SELECT cp FROM ChatParticipant cp
    WHERE cp.user.id = :userId
    AND cp.chatRoom.group.id = :groupId
    AND cp.isActive = true
""")
    List<ChatParticipant> findByUserAndGroupAndIsActive(@Param("userId") Long userId, @Param("groupId") Long groupId);

    // 채팅방 참여자 명단 조회
    @EntityGraph(attributePaths = {"user"}) // 참가자 + 유저 한 번에 패치
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

    // 유저가 특정 채팅방에 참여 중인지 확인
    @Query("""
    SELECT cp FROM ChatParticipant cp
    WHERE cp.user.id = :userId
    AND cp.chatRoom.id = :roomId
    AND cp.chatRoom.group.id = :groupId
    AND cp.isActive = true
""")
    Optional<ChatParticipant> findByUserAndChatRoom_IdAndChatRoom_Group_IdAndIsActiveTrue(
            @Param("userId") Long userId,
            @Param("roomId") Long roomId,
            @Param("groupId") Long groupId
    );

@Query("""
SELECT cp FROM ChatParticipant cp
WHERE cp.chatRoom.id = :roomId
  AND cp.user.id     = :userId
  AND cp.isActive    = true
""")
Optional<ChatParticipant> findActiveByRoomAndUser(@Param("roomId") Long roomId,
                                                  @Param("userId") Long userId);

}
