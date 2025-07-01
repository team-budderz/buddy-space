package team.budderz.buddyspace.global.exception;

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

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseErrorResponse> handleBizError(BaseException exception) {
        return ResponseEntity
                .status(exception.getErrorCode().getStatus())
                .body(new BaseErrorResponse(exception.getErrorCode()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseErrorResponse> handleNotFound(NoHandlerFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(BaseErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .code("API_NOT_FOUND")
                        .message("요청한 API가 존재하지 않습니다.")
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseErrorResponse> handleValidationError(MethodArgumentNotValidException exception) {
        String message;
        if (!exception.getBindingResult().getFieldErrors().isEmpty()) {
            message = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        } else if (!exception.getBindingResult().getGlobalErrors().isEmpty()) {
            message = exception.getBindingResult().getGlobalErrors().get(0).getDefaultMessage();
        } else {
            message = "입력값이 유효하지 않습니다.";
        }

        return ResponseEntity
                .badRequest()
                .body(BaseErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code("VALIDATION_FAILED")
                        .message(message)
                        .build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception) {
        return ResponseEntity
                .badRequest()
                .body(BaseErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code("MISSING_PARAMETER")
                        .message(String.format("필수 파라미터 '%s'가 없습니다.", exception.getParameterName()))
                        .build());
    }
}
