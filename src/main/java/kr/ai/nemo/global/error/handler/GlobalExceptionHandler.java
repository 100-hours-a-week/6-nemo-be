package kr.ai.nemo.global.error.handler;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import kr.ai.nemo.domain.auth.exception.AuthException;
import kr.ai.nemo.global.common.BaseApiResponse;
import kr.ai.nemo.global.error.code.CommonErrorCode;
import kr.ai.nemo.global.error.exception.CustomException;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.groupparticipants.exception.GroupParticipantException;
import kr.ai.nemo.domain.schedule.exception.ScheduleException;
import kr.ai.nemo.domain.scheduleparticipants.exception.ScheduleParticipantException;
import kr.ai.nemo.domain.user.exception.UserException;
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
  public ResponseEntity<BaseApiResponse<?>> handleBaseException(CustomException e) {
    return ResponseEntity
        .status(e.getCommonErrorCode().getHttpStatus())
        .body(BaseApiResponse.error(e.getCommonErrorCode()));
  }

  // 필수값 존재 X
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
    String errorMessage = e.getBindingResult().getFieldErrors().stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .filter(Objects::nonNull) // null 방지
        .findFirst()
        .orElse(CommonErrorCode.INVALID_REQUEST.getMessage());

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(BaseApiResponse.error(
            CommonErrorCode.INVALID_REQUEST.getHttpStatus().value(),  // 상태 코드 숫자만 추출
            errorMessage
        ));
  }


  // 파라미터가 없는 경우
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<BaseApiResponse<?>> handleMissingParamException(MissingServletRequestParameterException e) {
    String paramName = e.getParameterName();  // 빠진 파라미터 이름
    String message = String.format(CommonErrorCode.MISSING_REQUIRED_PARAMETER.getMessage(), paramName);

    BaseApiResponse<?> response = BaseApiResponse.error(
        CommonErrorCode.MISSING_REQUIRED_PARAMETER.getHttpStatus().value(),
        message
    );

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<BaseApiResponse<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(BaseApiResponse.error(CommonErrorCode.INVALID_ENUM));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseApiResponse<?>> handleGeneralException(Exception e, HttpServletRequest request) {
    Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    int statusCode = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR.value();

    return ResponseEntity
        .status(statusCode)
        .body(BaseApiResponse.error(statusCode, e.getMessage()));
  }

  @ExceptionHandler(AuthException.class)
  public ResponseEntity<BaseApiResponse<Object>> handleOAuthException(AuthException e) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(BaseApiResponse.error(e.getErrorCode()));
  }

  @ExceptionHandler(UserException.class)
  public ResponseEntity<BaseApiResponse<?>> handleUserException(UserException e) {
    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(BaseApiResponse.error(e.getErrorCode()));
  }

  @ExceptionHandler(GroupException.class)
  public ResponseEntity<BaseApiResponse<?>> handleGroupException(GroupException e) {
    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(BaseApiResponse.error(e.getErrorCode()));
  }

  @ExceptionHandler(GroupParticipantException.class)
  public ResponseEntity<BaseApiResponse<?>> handleGroupParticipantException(GroupParticipantException e) {
    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(BaseApiResponse.error(e.getErrorCode()));
  }

  @ExceptionHandler(ScheduleException.class)
  public ResponseEntity<BaseApiResponse<?>> handleScheduleException(ScheduleException e) {
    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(BaseApiResponse.error(e.getErrorCode()));
  }

  @ExceptionHandler(ScheduleParticipantException.class)
  public ResponseEntity<BaseApiResponse<?>> handleScheduleParticipantException(ScheduleParticipantException e) {
    return ResponseEntity
        .status(e.getErrorCode().getHttpStatus())
        .body(BaseApiResponse.error(e.getErrorCode()));
  }


  @ExceptionHandler(RestClientException.class)
  public ResponseEntity<BaseApiResponse<Object>> handleRestClientException(RestClientException e) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(BaseApiResponse.error(CommonErrorCode.EXTERNAL_API_ERROR));
  }
}
