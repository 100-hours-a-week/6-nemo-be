package kr.ai.nemo.common.exception;

import kr.ai.nemo.common.response.ApiResponse;
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
  public ResponseEntity<ApiResponse<?>> handleBaseException(CustomException e) {
    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(ApiResponse.error(e.getErrorCode()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ErrorCode.INVALID_REQUEST));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<?>> handleMissingParamException(MissingServletRequestParameterException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ErrorCode.MISSING_REQUIRED_PARAMETER));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(ErrorCode.INVALID_ENUM));
  }
  
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception e) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
  }
}