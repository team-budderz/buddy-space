package team.budderz.buddyspace.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.budderz.buddyspace.global.response.BaseErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseErrorResponse> handleBizError(BaseException exception) {
        return ResponseEntity
                .status(exception.getErrorCode().getStatus())
                .body(new BaseErrorResponse(exception.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseErrorResponse> handleValidationError(MethodArgumentNotValidException exception) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String code = "VALIDATION_FAILED";
        String message = exception.getBindingResult().getFieldError().getDefaultMessage();

        return ResponseEntity
                 .status(status.value())
                .body(BaseErrorResponse.builder()
                        .status(status.value())
                        .code(code)
                        .message(message)
                        .build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception) {
        String missingParam = exception.getParameterName();
        String message = String.format("필수 파라미터 '%s'가 없습니다.", missingParam);
        return ResponseEntity.badRequest().body(message);
    }

}
