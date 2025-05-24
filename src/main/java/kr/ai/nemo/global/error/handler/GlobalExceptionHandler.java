package kr.ai.nemo.global.error.handler;

import java.util.Objects;
import kr.ai.nemo.auth.exception.AuthException;
import kr.ai.nemo.global.common.ApiResponse;
import kr.ai.nemo.global.error.code.CommonErrorCode;
import kr.ai.nemo.global.error.exception.CustomException;
import kr.ai.nemo.group.exception.GroupException;
import kr.ai.nemo.groupparticipants.exception.GroupParticipantException;
import kr.ai.nemo.schedule.exception.ScheduleException;
import kr.ai.nemo.scheduleparticipants.exception.ScheduleParticipantException;
import kr.ai.nemo.user.exception.UserException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<ApiResponse<?>> handleBaseException(CustomException e) {
    return ResponseEntity
        .status(e.getCommonErrorCode().getHttpStatus())
        .body(ApiResponse.error(e.getCommonErrorCode()));
  }

  // 필수값 존재 X
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
    String errorMessage = e.getBindingResult().getFieldErrors().stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .filter(Objects::nonNull) // null 방지
        .findFirst()
        .orElse(CommonErrorCode.INVALID_REQUEST.getMessage());

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(
            CommonErrorCode.INVALID_REQUEST.getHttpStatus().value(),  // 상태 코드 숫자만 추출
            errorMessage
        ));
  }


  // 파라미터가 없는 경우
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<?>> handleMissingParamException(MissingServletRequestParameterException e) {
    String paramName = e.getParameterName();  // 빠진 파라미터 이름
    String message = String.format(CommonErrorCode.MISSING_REQUIRED_PARAMETER.getMessage(), paramName);

    ApiResponse<?> response = ApiResponse.error(
        CommonErrorCode.MISSING_REQUIRED_PARAMETER.getHttpStatus().value(),
        message
    );

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error(CommonErrorCode.INVALID_ENUM));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception e) {
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR));
  }

  @ExceptionHandler(AuthException.class)
  public ResponseEntity<ApiResponse<Object>> handleOAuthException(AuthException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(e.getErrorCode()));
  }

  @ExceptionHandler(UserException.class)
  public ResponseEntity<ApiResponse<?>> handleUserException(UserException e) {
    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(ApiResponse.error(e.getErrorCode()));
  }

  @ExceptionHandler(GroupException.class)
  public ResponseEntity<ApiResponse<?>> handleGroupException(GroupException e) {
    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(ApiResponse.error(e.getErrorCode()));
  }

  @ExceptionHandler(GroupParticipantException.class)
  public ResponseEntity<ApiResponse<?>> handleGroupParticipantException(GroupParticipantException e) {
    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(ApiResponse.error(e.getErrorCode()));
  }

  @ExceptionHandler(ScheduleException.class)
  public ResponseEntity<ApiResponse<?>> handleScheduleException(ScheduleException e) {
    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(ApiResponse.error(e.getErrorCode()));
  }

  @ExceptionHandler(ScheduleParticipantException.class)
  public ResponseEntity<ApiResponse<?>> handleScheduleParticipantException(ScheduleParticipantException e) {
    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(ApiResponse.error(e.getErrorCode()));
  }


  @ExceptionHandler(RestClientException.class)
  public ResponseEntity<ApiResponse<Object>> handleRestClientException(RestClientException e) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(ApiResponse.error(CommonErrorCode.EXTERNAL_API_ERROR));
  }
}
