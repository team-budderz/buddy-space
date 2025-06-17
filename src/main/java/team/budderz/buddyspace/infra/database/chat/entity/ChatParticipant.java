package team.budderz.buddyspace.infra.database.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ChatParticipantId.class)
@Table(name = "chat_participant")
public class ChatParticipant extends BaseEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "last_read_message_id", nullable = false)
    @Builder.Default
    private long lastReadMessageId  = 0L;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /** 단건 (실시간)  */
    public void updateLastRead(Long messageId) {
        // NULL 보호 + 뒤로 가는 값 무시
        if (messageId != null && messageId > lastReadMessageId) {
            this.lastReadMessageId = messageId;
        }
    }

    /** 일괄 동기화용(재접속·무한스크롤)  */
    public void syncLastRead(Long incoming) {
        if (incoming != null && incoming > lastReadMessageId) {
            this.lastReadMessageId = incoming;
        }
    }

    public void leave() {
        this.isActive = false;
        this.leftAt = LocalDateTime.now();
    }
}
