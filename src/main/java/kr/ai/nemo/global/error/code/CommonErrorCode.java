package kr.ai.nemo.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum CommonErrorCode implements ErrorCode {

  // 400 BAD REQUEST
  INVALID_REQUEST("INVALID_REQUEST", "입력값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
  INVALID_ENUM("INVALID_ENUM", "올바르지 않은 타입입니다.", HttpStatus.BAD_REQUEST),
  MISSING_REQUIRED_PARAMETER("MISSING_REQUIRED_PARAMETER", "'%s' 파라미터가 누락되었습니다.", HttpStatus.BAD_REQUEST),

  // 404 NOT FOUND
  USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  // 500 INTERNAL SERVER ERROR
  INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  S3_UPLOAD_FAILED("S3_UPLOAD_FAILED", "이미지 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  AI_SERVER_CONNECTION_FAILED("AI_SERVER_CONNECTION_FAILED", "AI 서버 연결에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  EXTERNAL_API_ERROR("EXTERNAL_API_ERROR", "외부 API 호출 중 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
  AI_RESPONSE_PARSE_ERROR("AI_RESPONSE_PARSE_ERROR", "AI 서버로부터 받은 응답을 처리할 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  REDIRECT_FAIL("REDIRECT_FAIL", "Redirect 실패", HttpStatus.INTERNAL_SERVER_ERROR );

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
