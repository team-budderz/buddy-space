package team.budderz.buddyspace.global.exception;

import lombok.Getter;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
public class BaseException extends RuntimeException {
    private final ErrorCode errorCode;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
