package team.budderz.buddyspace.global.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
@JsonPropertyOrder({"status", "code", "message", "timestamp"})
public class BaseErrorResponse implements ErrorCode {
    private final int status;
    private final String code;
    private final String message;

    public BaseErrorResponse(ErrorCode status){
        this.status = status.getStatus();
        this.code = status.getCode();
        this.message = status.getMessage();
    }
}
