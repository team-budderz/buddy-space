package team.budderz.buddyspace.domain.attachment.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AttachmentErrorCode implements ErrorCode {

    ATTACHMENT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "A001", "파일 데이터를 찾을 수 없습니다."),
    THUMBNAIL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "A002", "썸네일 생성에 실패했습니다."),
    INVALID_TYPE(HttpStatus.BAD_REQUEST.value(), "A003", "type 파라미터는 'image' 또는 'video'만 허용됩니다."),
    EMPTY_ATTACHMENT_ID_LIST(HttpStatus.BAD_REQUEST.value(), "A004", "삭제할 첨부파일 ID 목록이 비어 있습니다.");

    private final int status;
    private final String code;
    private final String message;
}
