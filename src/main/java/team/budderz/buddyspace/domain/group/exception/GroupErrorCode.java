package team.budderz.buddyspace.domain.group.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum GroupErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "U000", "사용자를 찾을 수 없습니다"), // 삭제 예정
    GROUP_TYPE_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "G001", "모임 유형을 찾을 수 없습니다."),
    INTEREST_NOT_FOUND(HttpStatus.BAD_REQUEST.value(), "G002", "관심사를 찾을 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
