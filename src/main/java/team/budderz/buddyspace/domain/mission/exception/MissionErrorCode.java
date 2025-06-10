package team.budderz.buddyspace.domain.mission.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@AllArgsConstructor
public enum MissionErrorCode implements ErrorCode {
    INVALID_MISSION_ID(HttpStatus.UNAUTHORIZED.value() ,"M001","유효하지 않은 미션 ID입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"M002","존재하지 않는 사용자입니다."),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"M003","존재하지 않는 모임입니다."),
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"M004","존재하지 않는 미션입니다."),
    MISSION_GROUP_MISMATCH(HttpStatus.FORBIDDEN.value() ,"M005","해당 모임에서 생성된 미션이 아닙니다."),
    MISSION_AUTHOR_MISMATCH(HttpStatus.FORBIDDEN.value() ,"M006","미션 생성자만 수정할 수 있습니다."),
    MISSION_LIMIT_EXCEEDED(HttpStatus.FORBIDDEN.value() ,"M007","미션은 1,000개까지 생성할 수 있습니다.")
    ;

    private final int status;
    private final String code;
    private final String message;
}
