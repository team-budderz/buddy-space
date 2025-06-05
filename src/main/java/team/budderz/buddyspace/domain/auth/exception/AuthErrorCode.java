package team.budderz.buddyspace.domain.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED.value() ,"A001","유효하지 않은 토큰입니다."),
    TOKEN_MISMATCH(HttpStatus.NOT_FOUND.value() ,"A002","토큰이 일치하지 않습니다.")
    ;

    private final int status;
    private final String code;
    private final String message;
}
