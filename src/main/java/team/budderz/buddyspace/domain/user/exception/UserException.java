package team.budderz.buddyspace.domain.user.exception;

import team.budderz.buddyspace.global.exception.BaseException;

public class UserException extends BaseException {
    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }
}
