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
    ;



    private final int status;
    private final String code;
    private final String message;
}
