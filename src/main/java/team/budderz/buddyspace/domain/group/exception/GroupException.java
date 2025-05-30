package team.budderz.buddyspace.domain.group.exception;

import team.budderz.buddyspace.global.exception.BaseException;

public class GroupException extends BaseException {
    public GroupException(GroupErrorCode errorCode) {
        super(errorCode);
    }
}
