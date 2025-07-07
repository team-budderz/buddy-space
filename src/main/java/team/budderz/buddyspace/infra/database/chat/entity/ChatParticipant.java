package team.budderz.buddyspace.infra.database.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.time.LocalDateTime;

/**
 * 채팅방에 참여한 사용자를 나타내는 엔티티입니다.
 * <ul>
 *     <li>복합 키(chatRoom + user)로 구성됩니다.</li>
 *     <li>각 참여자는 입장 시각, 퇴장 시각, 마지막 읽은 메시지 ID 등의 정보를 가집니다.</li>
 *     <li>퇴장 시 isActive = false 로 처리됩니다.</li>
 * </ul>
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ChatParticipantId.class)
@Table(name = "chat_participant")
public class ChatParticipant extends BaseEntity {

    /** 참여한 채팅방 */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /** 참여 사용자 */
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 입장 시각 */
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    /** 퇴장 시각 (null이면 아직 퇴장하지 않음) */
    @Column(name = "left_at")
    private LocalDateTime leftAt;

    /** 마지막으로 읽은 메시지 ID */
    @Column(name = "last_read_message_id", nullable = false)
    @Builder.Default
    private long lastReadMessageId = 0L;

    /** 현재 방에 활성 상태인지 여부 */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * 메시지 ID 기준으로 마지막 읽음 상태를 단건 업데이트합니다.
     * - 실시간 메시지 수신 시 사용됩니다.
     *
     * @param messageId 읽은 메시지 ID
     */
    public void updateLastRead(Long messageId) {
        // NULL 보호 + 뒤로 가는 값 무시
        if (messageId != null && messageId > lastReadMessageId) {
            this.lastReadMessageId = messageId;
        }
    }

    /**
     * 메시지 목록 스크롤 시, 클라이언트에서 보낸 최종 읽은 ID로 동기화합니다.
     *
     * @param incoming 최종 읽은 메시지 ID
     */
    public void syncLastRead(Long incoming) {
        if (incoming != null && incoming > lastReadMessageId) {
            this.lastReadMessageId = incoming;
        }
    }

    /**
     * 채팅방을 나간 것으로 표시합니다.
     * - isActive = false
     * - leftAt 시각 기록
     */
    public void leave() {
        if (this.isActive) {
            this.isActive = false;
            if (this.leftAt == null) { // 최초 퇴장 시간 보존
                this.leftAt = LocalDateTime.now();
            }
        }
    }

}
