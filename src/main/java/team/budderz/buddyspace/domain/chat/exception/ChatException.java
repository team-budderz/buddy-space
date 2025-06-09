package team.budderz.buddyspace.domain.chat.exception;

import team.budderz.buddyspace.global.exception.BaseException;

public class ChatException extends BaseException {
    public ChatException(ChatErrorCode errorCode) {
        super(errorCode);
    }
}
