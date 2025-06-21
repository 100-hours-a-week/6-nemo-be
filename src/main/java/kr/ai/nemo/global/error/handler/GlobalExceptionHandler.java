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
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomException.class)
  public ResponseEntity<BaseApiResponse<?>> handleBaseException(CustomException e) {
    return ResponseEntity
        .status(e.getCommonErrorCode().getHttpStatus())
        .body(BaseApiResponse.error(e.getCommonErrorCode()));
  }

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

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<BaseApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    String message = String.format("잘못된 파라미터입니다. '%s'는 '%s' 타입으로 변환할 수 없습니다.",
        e.getValue(), e.getRequiredType() != null);

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(BaseApiResponse.error(HttpStatus.BAD_REQUEST.value(), message));
  }

  @ExceptionHandler(MissingRequestCookieException.class)
  public ResponseEntity<BaseApiResponse<?>> handleMissingCookie(MissingRequestCookieException e) {
    String message = String.format("필수 쿠키가 누락되었습니다: %s", e.getCookieName());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(BaseApiResponse.error(
            HttpStatus.BAD_REQUEST.value(),
            message
        ));
  }

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

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<BaseApiResponse<?>> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
    String message = "지원하지 않는 Content-Type입니다: " + e.getContentType();
    return ResponseEntity
        .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(BaseApiResponse.error(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            message
        ));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<BaseApiResponse<?>> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
    String message = String.format("지원하지 않는 HTTP 메서드입니다: %s", e.getMethod());
    return ResponseEntity
        .status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(BaseApiResponse.error(
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            message
        ));
  }

  @ExceptionHandler(RestClientException.class)
  public ResponseEntity<BaseApiResponse<Object>> handleRestClientException(RestClientException e) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(BaseApiResponse.error(CommonErrorCode.EXTERNAL_API_ERROR));
  }
}
