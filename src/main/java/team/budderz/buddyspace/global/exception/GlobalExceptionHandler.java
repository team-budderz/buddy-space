package team.budderz.buddyspace.global.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import team.budderz.buddyspace.global.response.BaseErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직에서 발생한 BaseException을 처리하여 적절한 HTTP 상태 코드와 에러 정보를 반환합니다.
     *
     * @param exception 처리할 BaseException 인스턴스
     * @return 예외의 에러 코드에 해당하는 HTTP 상태와 BaseErrorResponse를 포함한 ResponseEntity
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseErrorResponse> handleBizError(BaseException exception) {
        return ResponseEntity
                .status(exception.getErrorCode().getStatus())
                .body(new BaseErrorResponse(exception.getErrorCode()));
    }

    /**
     * 존재하지 않는 API 요청 시 404 오류와 함께 표준 에러 응답을 반환합니다.
     *
     * @param ex 처리할 NoHandlerFoundException 예외
     * @return 404 상태 코드와 "API_NOT_FOUND" 에러 코드, 안내 메시지가 포함된 BaseErrorResponse
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseErrorResponse> handleNotFound(NoHandlerFoundException ex) {
        return ResponseEntity
                .status(404)
                .body(BaseErrorResponse.builder()
                        .status(404)
                        .code("API_NOT_FOUND")
                        .message("요청한 API가 존재하지 않습니다.")
                        .build());
    }

    /**
     * 유효하지 않은 요청 파라미터로 인해 발생한 검증 예외를 처리하여 표준 에러 응답을 반환합니다.
     *
     * @param exception 유효성 검사 실패 시 발생하는 예외 객체
     * @return 400 Bad Request 상태와 검증 실패 코드 및 메시지를 포함한 에러 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseErrorResponse> handleValidationError(MethodArgumentNotValidException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String code = "VALIDATION_FAILED";
        String message;
        if (!exception.getBindingResult().getFieldErrors().isEmpty()) {
            message = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        } else if (!exception.getBindingResult().getGlobalErrors().isEmpty()) {
            message = exception.getBindingResult().getGlobalErrors().get(0).getDefaultMessage();
        } else {
            message = "입력값이 유효하지 않습니다.";
        }

        return ResponseEntity
                .status(status.value())
                .body(BaseErrorResponse.builder()
                        .status(status.value())
                        .code(code)
                        .message(message)
                        .build());
    }

    /**
     * 필수 요청 파라미터가 누락된 경우 400 Bad Request와 표준화된 에러 응답을 반환합니다.
     *
     * @param exception 누락된 요청 파라미터 예외
     * @return 누락된 파라미터 정보를 포함한 BaseErrorResponse와 400 상태 코드
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception) {
        return ResponseEntity
                .badRequest()
                .body(BaseErrorResponse.builder()
                        .status(HttpServletResponse.SC_BAD_REQUEST)
                        .code("MISSING_PARAMETER")
                        .message(String.format("필수 파라미터 '%s'가 없습니다.", exception.getParameterName()))
                        .build());
    }
}
