package kr.ai.nemo.exception.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ErrorResponse> handleBaseException(CustomException e) {
    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(ErrorResponse.of(e.getErrorCode()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParamException(MethodArgumentNotValidException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of(ErrorCode.MISSING_REQUIRED_PARAMETER));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.of(ErrorCode.INVALID_ENUM));
  }
}
