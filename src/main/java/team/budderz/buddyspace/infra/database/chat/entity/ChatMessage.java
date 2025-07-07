package team.budderz.buddyspace.infra.database.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import team.budderz.buddyspace.global.entity.BaseEntity;
import team.budderz.buddyspace.infra.database.user.entity.User;
import team.budderz.buddyspace.infra.database.chat.entity.MessageType;

import java.awt.*;
import java.time.LocalDateTime;

/**
 * 채팅 메시지를 나타내는 JPA 엔티티입니다.
 * <ul>
 *     <li>TEXT, IMAGE 등 다양한 메시지 유형을 지원합니다.</li>
 *     <li>보낸 사용자(sender) 및 채팅방(chatRoom)과 연관됩니다.</li>
 *     <li>첨부파일 URL은 문자열 형태로 저장됩니다.</li>
 * </ul>
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "chat_message")
public class ChatMessage extends BaseEntity {

    /** 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 해당 메시지가 속한 채팅방 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    /** 메시지를 보낸 사용자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User sender;

    /** 메시지 유형 (TEXT, IMAGE 등) */
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    /** 메시지 내용 (TEXT 타입일 경우 사용) */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 메시지 전송 시각 */
    private LocalDateTime sentAt;

    /** 첨부파일 URL (이미지 등), 연관관계 없이 URL 문자열만 저장 */
    @Column(name = "attachment_url")
    private String attachmentUrl;

}
