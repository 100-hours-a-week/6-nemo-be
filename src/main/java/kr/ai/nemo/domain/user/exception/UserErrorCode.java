package kr.ai.nemo.domain.user.exception;

import kr.ai.nemo.global.error.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum UserErrorCode implements ErrorCode {

  // 404 NOT FOUND
  USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  // 409 Conflict
  USER_WITHDRAWN("USER_WITHDRAWN", "탈퇴한 사용자입니다.", HttpStatus.CONFLICT);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
