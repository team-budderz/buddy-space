package team.budderz.buddyspace.domain.neighborhood.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import team.budderz.buddyspace.global.response.ErrorCode;

@Getter
@AllArgsConstructor
public enum NeighborhoodErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"N001","존재하지 않는 사용자 입니다."),
    NEIGHBORHOOD_NOT_FOUND(HttpStatus.NOT_FOUND.value() ,"N002","존재하지 않는 동네 입니다."),
    USER_NEIGHBORHOOD_MISS_MATCH(HttpStatus.NOT_FOUND.value() ,"N003","사용자가 인증한 동네가 아닙니다.")
    ;

    private final int status;
    private final String code;
    private final String message;
}
