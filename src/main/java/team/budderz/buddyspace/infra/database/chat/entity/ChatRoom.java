package team.budderz.buddyspace.infra.database.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.group.entity.Group;
import team.budderz.buddyspace.infra.database.user.entity.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 채팅방(ChatRoom) 엔티티입니다.
 * <p>
 * 채팅방의 이름, 설명, 타입, 소속 그룹, 생성자, 메시지, 참가자 정보를 포함합니다.
 * 그룹별로 다수의 채팅방을 생성할 수 있으며,
 * 각 채팅방에는 다수의 사용자(ChatParticipant)와 메시지(ChatMessage)가 속합니다.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_room")
public class ChatRoom extends BaseEntity {

    /**
     * 채팅방 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 채팅방 이름
     */
    private String name;

    /**
     * 채팅방 설명
     */
    private String description;

    /**
     * 채팅방 유형 (예: 일반, 그룹, 공지 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "chat_room_type")
    private ChatRoomType chatRoomType;

    /**
     * 채팅방이 속한 그룹
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    /**
     * 채팅방을 생성한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User createdBy;

    /**
     * 채팅방에 등록된 메시지 목록
     * <p>
     * {@code chatMessage.chatRoom}을 기준으로 양방향 매핑되며,
     * 채팅방 삭제 시 메시지들도 함께 삭제됩니다.
     */
    @OneToMany(
            mappedBy = "chatRoom",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * 채팅방에 참여 중인 사용자 목록
     * <p>
     * {@code chatParticipant.chatRoom}을 기준으로 양방향 매핑되며,
     * 채팅방 삭제 시 참가 정보도 함께 삭제됩니다.
     */
    @OneToMany(
            mappedBy = "chatRoom",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ChatParticipant> participants = new ArrayList<>();

    /**
     * 채팅방 정보를 수정합니다.
     *
     * @param name        채팅방 이름
     * @param description 채팅방 설명
     */
    public void updateInfo(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
