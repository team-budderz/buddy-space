package team.budderz.buddyspace.infra.database.chat.entity;

/**
 * 채팅방의 유형을 나타내는 열거형입니다.
 * <ul>
 *     <li>{@link #GROUP} - 그룹 채팅방 (여러 명 참여 가능)</li>
 *     <li>{@link #DIRECT} - 1:1 채팅방 (두 명만 참여)</li>
 * </ul>
 */
public enum ChatRoomType {
    GROUP, DIRECT
}
