package team.budderz.buddyspace.domain.post.exception;

import team.budderz.buddyspace.global.exception.BaseException;

public class PostException extends BaseException {
    public PostException(PostErrorCode errorCode) {
        super(errorCode);
    }
}
