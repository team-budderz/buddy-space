package team.budderz.buddyspace.domain.group.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum GroupErrorCode implements ErrorCode {

    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "G001", "해당 모임을 찾을 수 없습니다."),
    GROUP_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "G002", "모임 유형을 찾을 수 없습니다."),
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "G003", "모임 유형을 찾을 수 없습니다."),
    FUNCTION_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "G004", "해당 기능에 대한 접근 권한이 없습니다."),
    MEMBERS_EXIST_IN_GROUP(HttpStatus.BAD_REQUEST.value(), "G005", "멤버가 존재하는 모임은 삭제할 수 없습니다.");

    private final int status;
    private final String code;
    private final String message;
}
