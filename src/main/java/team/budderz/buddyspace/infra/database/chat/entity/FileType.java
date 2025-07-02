package team.budderz.buddyspace.infra.database.chat.entity;

/**
 * 채팅 메시지에 첨부된 파일의 유형을 구분하는 열거형입니다.
 * <ul>
 *     <li>{@link #IMAGE} - 이미지 파일 (.jpg, .png 등)</li>
 *     <li>{@link #VIDEO} - 동영상 파일 (.mp4, .avi 등)</li>
 *     <li>{@link #FILE}  - 일반 파일 (PDF, ZIP 등)</li>
 * </ul>
 */
public enum FileType {
    IMAGE, VIDEO, FILE
}
