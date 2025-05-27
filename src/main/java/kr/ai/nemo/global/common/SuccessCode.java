package kr.ai.nemo.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum SuccessCode {

  SUCCESS("SUCCESS", "요청이 성공적으로 처리되었습니다.", HttpStatus.OK),
  CREATED("CREATED", "리소스가 성공적으로 생성되었습니다.", HttpStatus.CREATED),
  NO_CONTENT("NO_CONTENT", "성공적으로 처리되었습니다.", HttpStatus.NO_CONTENT);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
