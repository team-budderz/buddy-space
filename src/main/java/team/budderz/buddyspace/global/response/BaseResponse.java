package team.budderz.buddyspace.global.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import static team.budderz.buddyspace.global.response.BaseErrorCode.SUCCESS;

@Getter
@JsonPropertyOrder({"status", "code", "message", "result"})
@Schema(description = "공통 성공 응답 DTO")
public class BaseResponse<T> implements ErrorCode {
    @Schema(description = "HTTP 상태 코드", example = "200")
    private final int status;

    @Schema(description = "응답 코드", example = "B001")
    private final String code;

    @Schema(description = "응답 메시지", example = "요청에 성공하였습니다.")
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "응답 결과 (결과가 없을 경우 응답 미포함)")
    private final T result;

    @JsonCreator
    public BaseResponse(T result){
        this.status = SUCCESS.getStatus();
        this.code = SUCCESS.getCode();
        this.message = SUCCESS.getMessage();
        this.result = result;
    }
}
