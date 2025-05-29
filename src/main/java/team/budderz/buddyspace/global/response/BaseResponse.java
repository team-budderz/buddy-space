package team.budderz.buddyspace.global.response;

import static team.budderz.buddyspace.global.response.BaseErrorCode.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"status", "code", "message", "result"})
public class BaseResponse<T> implements ErrorCode {
    private final int status;
    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final T result;

    @JsonCreator
    public BaseResponse(T result){
        this.status = SUCCESS.getStatus();
        this.code = SUCCESS.getCode();
        this.message = SUCCESS.getMessage();
        this.result = result;
    }
}
