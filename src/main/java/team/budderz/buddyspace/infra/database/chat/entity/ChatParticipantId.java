package team.budderz.buddyspace.infra.database.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * {@link ChatParticipant}의 복합 기본 키 클래스입니다.
 * <p>
 * chatRoom 과 user 필드를 기준으로 복합 키를 구성하며,
 * JPA 에서 {@code @IdClass}를 사용할 때 필수적으로 {@code equals}와 {@code hashCode}를 재정의해야
 * 정확한 엔티티 식별 및 조회가 가능합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipantId implements Serializable {

    /**
     * 채팅방 ID (참조용)
     */
    private Long chatRoom;

    /**
     * 사용자 ID (참조용)
     */
    private Long user;

    /**
     * 두 객체가 동일한 복합 키인지 비교합니다.
     * chatRoom, user 값이 동일하면 같은 키로 간주합니다.
     *
     * @param o 비교할 객체
     * @return 동일한 키이면 true, 아니면 false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatParticipantId)) return false;
        ChatParticipantId that = (ChatParticipantId) o;
        return Objects.equals(chatRoom, that.chatRoom) &&
                Objects.equals(user, that.user);
    }

    /**
     * chatRoom 과 user 필드를 기반으로 해시 코드를 생성합니다.
     *
     * @return 해시값
     */
    @Override
    public int hashCode() {
        return Objects.hash(chatRoom, user);
    }
}
