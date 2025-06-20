package team.budderz.buddyspace.domain.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"U001","존재하지 않는 유저 입니다."),
    INVALID_USER_EMAIL(HttpStatus.UNAUTHORIZED.value() ,"U002","유효하지 않은 사용자 이메일입니다."),
    INVALID_USER_PASSWORD(HttpStatus.UNAUTHORIZED.value() ,"U003","유효하지 않은 사용자 비밀번호입니다."),
    INVALID_USER_REQUEST(HttpStatus.BAD_REQUEST.value() ,"U004","유효하지 않은 사용자 요청입니다."),
    INVALID_USER_ID(HttpStatus.UNAUTHORIZED.value() ,"U005","유효하지 않은 사용자 ID입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.NOT_FOUND.value() ,"U006","비밀번호가 일치하지 않습니다."),
    USER_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value() ,"U007","사용자 탈퇴에 실패했습니다.")
    ;


    private final int status;
    private final String code;
    private final String message;
}
