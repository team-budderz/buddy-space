package team.budderz.buddyspace.domain.post.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@AllArgsConstructor
public enum PostErrorCode implements ErrorCode {
    GROUP_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"P001","존재하지 않는 그룹 입니다.")
    , USER_ID_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"P002","존재하지 않는 유저 입니다."),


    ;


    private final int status;
    private final String code;
    private final String message;
}
