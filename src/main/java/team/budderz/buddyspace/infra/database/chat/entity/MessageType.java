package team.budderz.buddyspace.infra.database.chat.entity;

/**
 * 채팅 메시지의 형식을 구분하는 열거형입니다.
 * <ul>
 *     <li>{@link #TEXT}   - 일반 텍스트 메시지</li>
 *     <li>{@link #IMAGE}  - 이미지 메시지</li>
 *     <li>{@link #VIDEO}  - 동영상 메시지</li>
 *     <li>{@link #FILE}   - 일반 파일 첨부 메시지</li>
 *     <li>{@link #SYSTEM} - 시스템 메시지 (예: 입장/퇴장 알림)</li>
 * </ul>
 */
public enum MessageType {
    TEXT, IMAGE, VIDEO, FILE, SYSTEM
}
