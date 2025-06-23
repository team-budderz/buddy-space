package team.budderz.buddyspace.domain.notification.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements ErrorCode {
    UNSUPPORTED_NOTIFICATION_TYPE(HttpStatus.BAD_REQUEST.value(), "N001", "지원하지 않는 알림 타입입니다.")
    , USER_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "N002", "존재하지 않는 유저입니다.")
    , NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "N003", "존재하지 않는 알람입니다.")
    , NO_AUTH_TO_READ_NOTIFICATION(HttpStatus.FORBIDDEN.value(), "N004", "해당 알림을 조회할 권한이 없습니다.")
    , INVALID_NOTIFICATION_ARGUMENT( HttpStatus.BAD_REQUEST.value(), "N005", "알림 URL 생성에 필요한 인자가 없습니다.");
    ;



    private final int status;
    private final String code;
    private final String message;
}
