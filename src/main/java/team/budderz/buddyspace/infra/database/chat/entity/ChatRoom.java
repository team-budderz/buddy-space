package team.budderz.buddyspace.infra.database.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_room")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "chat_room_type")
    private ChatRoomType chatRoomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * 채팅방에 속한 메시지들.
     * chatMessage.chatRoom 필드 기준으로 매핑, 삭제 시 연관 메시지 자동 제거
     */
    @OneToMany(
            mappedBy = "chatRoom",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * 채팅방에 속한 참가자들.
     * chatParticipant.chatRoom 필드 기준으로 매핑, 삭제 시 연관 참가자 자동 제거
     */
    @OneToMany(
            mappedBy = "chatRoom",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ChatParticipant> participants = new ArrayList<>();

    public void updateInfo(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
