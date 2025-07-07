package team.budderz.buddyspace.infra.database.chat.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipant;
import team.budderz.buddyspace.infra.database.chat.entity.ChatParticipantId;
import team.budderz.buddyspace.infra.database.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * {@link ChatParticipant} 엔티티에 대한 JPA Repository입니다.
 * - 채팅방 참여자 조회, 읽음 위치 업데이트, 참여 상태 확인 등의 기능을 제공합니다.
 */
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ChatParticipantId> {

    /**
     * 사용자가 특정 그룹 내에서 현재 참여 중인 모든 채팅방 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param groupId 그룹 ID
     * @return 참여 중인 채팅방 리스트
     */
    @Query("""
        SELECT cp FROM ChatParticipant cp
        WHERE cp.user.id = :userId
        AND cp.chatRoom.group.id = :groupId
        AND cp.isActive = true
    """)
    List<ChatParticipant> findByUserAndGroupAndIsActive(@Param("userId") Long userId, @Param("groupId") Long groupId);

    /**
     * 해당 채팅방의 모든 참가자 목록을 조회합니다. (User 엔티티까지 즉시 로딩)
     *
     * @param chatRoom 채팅방
     * @return 참여자 리스트
     */
    @EntityGraph(attributePaths = {"user"})
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

    /**
     * 특정 사용자가 채팅방에 참여 중인지 확인합니다.
     *
     * @param userId 사용자 ID
     * @param roomId 채팅방 ID
     * @param groupId 그룹 ID
     * @return 참여자 Optional
     */
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

    /**
     * 특정 채팅방에서 해당 사용자의 참여 정보를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @return 참여자 Optional
     */
    @Query("""
        SELECT cp FROM ChatParticipant cp
        WHERE cp.chatRoom.id = :roomId
          AND cp.user.id     = :userId
          AND cp.isActive    = true
    """)
    Optional<ChatParticipant> findActiveByRoomAndUser(@Param("roomId") Long roomId,
                                                      @Param("userId") Long userId);

    /**
     * 채팅방 ID 기준으로 모든 활성화된 참여자를 조회합니다. (User 포함)
     *
     * @param roomId 채팅방 ID
     * @return 참여자 리스트
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("""
        SELECT cp FROM ChatParticipant cp
        WHERE cp.chatRoom.id = :roomId
        AND cp.isActive = true
    """)
    List<ChatParticipant> findByChatRoomId(@Param("roomId") Long roomId);

    /**
     * 채팅방 ID 기준으로 모든 활성화된 참여자를 조회합니다.
     *
     * @param roomId 채팅방 ID
     * @return 참여자 리스트
     */
    @Query("""
        SELECT p FROM ChatParticipant p
        WHERE p.chatRoom.id = :roomId
        AND p.isActive = true
    """)
    List<ChatParticipant> findActiveByRoom(@Param("roomId") Long roomId);

    /**
     * 사용자의 마지막 읽은 메시지 ID를 업데이트합니다.
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     * @param lastReadMessageId 마지막으로 읽은 메시지 ID
     * @return 수정된 행 수
     */
    @Modifying
    @Query("""
        UPDATE ChatParticipant p
        SET p.lastReadMessageId = :lastRead
        WHERE p.chatRoom.id = :roomId
          AND p.user.id     = :userId
          AND p.isActive    = true
    """)
    int updateLastRead(
            @Param("roomId") Long roomId,
            @Param("userId") Long userId,
            @Param("lastRead") Long lastReadMessageId
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM ChatParticipant cp WHERE cp.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(cp) FROM ChatParticipant cp WHERE cp.chatRoom.id = :roomId AND cp.isActive = true")
    long countActiveParticipantsByRoomId(@Param("roomId") Long roomId);

    @Query("""
    SELECT DISTINCT cp.chatRoom.id
    FROM ChatParticipant cp
    WHERE cp.user.id = :userId
      AND cp.isActive = true
    """)
    List<Long> findActiveRoomIdsByUserId(@Param("userId") Long userId);


}
