package team.budderz.buddyspace.infra.database.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @Column(name = "chat_room_id")
    private Long chatRoom;

    @Id
    @Column(name = "user_id")
    private Long user;

    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    private Long lastReadMessageId;

    private boolean isActive = true;
}
