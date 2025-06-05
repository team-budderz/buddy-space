package team.budderz.buddyspace.domain.auth.exception;

import team.budderz.buddyspace.global.exception.BaseException;

public class AuthException extends BaseException {
    public AuthException(AuthErrorCode authErrorCode) {
        super(authErrorCode);
    }
}
