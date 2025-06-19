package team.budderz.buddyspace.domain.attachment.exception;

import team.budderz.buddyspace.global.exception.BaseException;

public class AttachmentException extends BaseException {
    public AttachmentException(AttachmentErrorCode errorCode) {
        super(errorCode);
    }
}
