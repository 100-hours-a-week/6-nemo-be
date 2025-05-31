package kr.ai.nemo.domain.auth.exception;

import kr.ai.nemo.global.error.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum AuthErrorCode implements ErrorCode {

  // 401 UNAUTHORIZED
  UNAUTHORIZED("UNAUTHORIZED", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
  INVALID_TOKEN("INVALID_TOKEN", "토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
  EXPIRED_TOKEN("EXPIRED_TOKEN", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),

  // 403 FORBIDDEN
  ACCESS_DENIED("NO_PERMISSION", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
