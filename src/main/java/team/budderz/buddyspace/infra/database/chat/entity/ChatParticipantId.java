package team.budderz.buddyspace.infra.database.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/* chatRoom, user 가 복합기본키이기 때문에, JPA 에서 구분할 수 있도록 선언
*
* JPA 에서 @IdClass 사용시, 엔티티 조회/비교할때 equals(), hashCode() 내부적으로 사용함
* --> equals(), hashCode() 를 재정의하지 않으면 서로 다른 객체로 인식돼 조회 실패 오류 발발
*/
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatParticipantId implements Serializable {

    private Long chatRoom;
    private Long user;

    // ChatParticipantId가 JPA 복합 키로서 정확하게 동작하도록 유도
    // 메모리 주소가 다르더라도 chatRoom, user 값이 같으면 동일한 참여자로 간주
    @Override
    public boolean equals(Object o) {
        // 두 객체의 메모리 주소 동일한 경우(같은 객체)
        if (this == o) return true;

        // 전달된 객체(o) 가 ChatParticipantId 타입이 아닌 경우(다른 객체)
        if (!(o instanceof ChatParticipantId)) return false;

        ChatParticipantId that = (ChatParticipantId) o;

        // 두 필드를 각각 비교 (Objects.equals: null-safe)
        return Objects.equals(chatRoom, that.chatRoom) &&
                Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatRoom, user);
    }
}
