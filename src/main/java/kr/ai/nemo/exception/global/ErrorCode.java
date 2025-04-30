package kr.ai.nemo.exception.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {

  // 400 BAD REQUEST
  INVALID_REQUEST("INVALID_REQUEST", "입력값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
  INVALID_ENUM("INVALID_ENUM", "올바르지 않은 타입입니다.", HttpStatus.BAD_REQUEST),
  MISSING_REQUIRED_PARAMETER("MISSING_REQUIRED_PARAMETER", "필수 요청 파라미터가 누락되었습니다.", HttpStatus.BAD_REQUEST),

  // 401 UNAUTHORIZED
  UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
  INVALID_TOKEN("INVALID_TOKEN", "토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
  EXPIRED_TOKEN("EXPIRED_TOKEN", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),

  // 403 FORBIDDEN
  ACCESS_DENIED("NO_PERMISSION", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

  // 404 NOT FOUND
  USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  // 500 INTERNAL SERVER ERROR
  INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  AI_SERVER_CONNECTION_FAILED("AI_SERVER_CONNECTION_FAILED", "AI 서버 연결에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);


  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
