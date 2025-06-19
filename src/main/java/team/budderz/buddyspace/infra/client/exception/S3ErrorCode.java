package team.budderz.buddyspace.infra.client.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum S3ErrorCode implements ErrorCode {

    FILE_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "S001", "업로드할 파일이 없습니다."),
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "S002", "S3 업로드에 실패했습니다."),
    DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "S003", "S3 삭제에 실패했습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST.value(), "S004", "파일 크기가 10MB를 초과했습니다."),
    GENERATE_URL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "S005", "Presigned URL 생성에 실패했습니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST.value(), "S006", "파일 이름이 유효하지 않습니다."),
    LIST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "S007", "S3 목록 조회에 실패했습니다."),
    CHECK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "S008", "S3 파일 확인 중 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
