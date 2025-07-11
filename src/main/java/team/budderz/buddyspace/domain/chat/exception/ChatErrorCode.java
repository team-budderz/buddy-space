package team.budderz.buddyspace.domain.chat.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import team.budderz.buddyspace.global.response.ErrorCode;

/**
 * Chat 관련 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "채팅 관련 에러 코드")
public enum ChatErrorCode implements ErrorCode {

    @Schema(description = "존재하지 않는 채팅방입니다.")
    CHAT_ROOM_NOT_FOUND(404, "C001", "존재하지 않는 채팅방입니다."),

    @Schema(description = "그룹에 참여하지 않은 사용자입니다.")
    USER_NOT_IN_GROUP(403, "C002", "그룹에 참여하지 않은 사용자입니다."),

    @Schema(description = "채팅방에 참여하지 않은 사용자입니다.")
    USER_NOT_IN_CHAT_ROOM(403, "C007", "채팅방에 참여하지 않은 사용자입니다."),

    @Schema(description = "존재하지 않는 사용자입니다.")
    USER_NOT_FOUND(404, "C003", "존재하지 않는 사용자입니다."),

    @Schema(description = "이미 존재하는 DIRECT 채팅방입니다.")
    DUPLICATE_DIRECT_ROOM(409, "C004", "이미 존재하는 DIRECT 채팅방입니다."),

    @Schema(description = "참여자 수가 올바르지 않습니다.")
    INVALID_PARTICIPANT_COUNT(400, "C005", "참여자 수가 올바르지 않습니다."),

    @Schema(description = "존재하지 않는 그룹입니다.")
    GROUP_NOT_FOUND(404, "C008", "존재하지 않는 그룹입니다."),

    @Schema(description = "존재하지 않는 메시지입니다.")
    MESSAGE_NOT_FOUND(404, "C006", "존재하지 않는 메시지입니다."),

    @Schema(description = "메시지가 해당 채팅방에 속하지 않습니다.")
    MESSAGE_NOT_IN_ROOM(404, "C009", "메시지가 해당 채팅방에 속하지 않습니다."),

    @Schema(description = "권한이 없습니다.")
    NO_PERMISSION(403, "C010", "권한이 없습니다."),

    @Schema(description = "이미 채팅방에 참여 중인 사용자입니다.")
    USER_ALREADY_IN_CHAT_ROOM(409, "C011", "이미 채팅방에 참여중인 사용자입니다."),

    @Schema(description = "본인은 강퇴할 수 없습니다.")
    CANNOT_KICK_SELF(400, "C012", "본인은 강퇴할 수 없습니다."),

    @Schema(description = "메시지 삭제 가능 시간이 지났습니다.")
    MESSAGE_DELETE_TIME_EXPIRED(400, "C013", "메시지 삭제 가능 시간이 지났습니다.");

    @Schema(description = "HTTP 상태 코드", example = "404")
    private final int status;

    @Schema(description = "에러 코드", example = "C001")
    private final String code;

    @Schema(description = "에러 메시지", example = "존재하지 않는 채팅방입니다.")
    private final String message;
}
