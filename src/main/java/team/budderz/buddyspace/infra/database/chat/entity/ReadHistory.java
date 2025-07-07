package team.budderz.buddyspace.infra.database.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import team.budderz.buddyspace.infra.database.user.entity.User;

@Entity
@Table(name = "read_history",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "chat_room_id"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReadHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long lastReadMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;
}
