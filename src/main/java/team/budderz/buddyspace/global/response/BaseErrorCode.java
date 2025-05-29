package team.budderz.buddyspace.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BaseErrorCode implements ErrorCode {
    SUCCESS(HttpStatus.OK.value(), "B001", "요청에 성공하였습니다.");

    private final int status;
    private final String code;
    private final String message;
}
