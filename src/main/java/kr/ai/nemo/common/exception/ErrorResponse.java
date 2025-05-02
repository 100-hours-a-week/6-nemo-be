package kr.ai.nemo.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
  private String code;
  private String message;
  private int status;
  private Object data;

  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(
        errorCode.getCode(),
        errorCode.getMessage(),
        errorCode.getHttpStatus().value(),
        null
    );
  }
}
