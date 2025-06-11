package team.budderz.buddyspace.domain.missionpost.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@AllArgsConstructor
public enum MissionPostErrorCode implements ErrorCode {
    INVALID_MISSION_POST_ID(HttpStatus.BAD_REQUEST.value() ,"M001","유효하지 않은 미션 ID입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"MP002","존재하지 않는 사용자입니다."),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"MP003","존재하지 않는 모임입니다."),
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"MP004","존재하지 않는 미션입니다."),
    MISSION_POST_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"MP005","존재하지 않는 미션인증입니다."),
    MISSION_MISMATCH(HttpStatus.FORBIDDEN.value() ,"MP006","해당 미션에서 생성된 미션인증이 아닙니다."),
    MISSION_POST_GROUP_MISMATCH(HttpStatus.FORBIDDEN.value() ,"MP007","해당 모임에서 생성된 미션이 아닙니다.")
    ;

    private final int status;
    private final String code;
    private final String message;
}
