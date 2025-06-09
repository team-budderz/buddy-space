package team.budderz.buddyspace.domain.chat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.global.response.ErrorCode;

/**
 * Chat 관련 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {

    CHAT_ROOM_NOT_FOUND(404, "C001", "존재하지 않는 채팅방입니다."),
    USER_NOT_IN_GROUP(403, "C002", "그룹에 참여하지 않은 사용자입니다."),
    USER_NOT_IN_CHAT_ROOM(403, "C007", "채팅방에 참여하지 않은 사용자입니다."),
    USER_NOT_FOUND(404, "C003", "존재하지 않는 사용자입니다."),
    DUPLICATE_DIRECT_ROOM(409, "C004", "이미 존재하는 DIRECT 채팅방입니다."),
    INVALID_PARTICIPANT_COUNT(400, "C005", "참여자 수가 올바르지 않습니다."),
    GROUP_NOT_FOUND(404, "C008", "존재하지 않는 그룹입니다."),
    MESSAGE_NOT_FOUND(404, "C006", "존재하지 않는 메시지입니다.");

    private final int status;
    private final String code;
    private final String message;
}
