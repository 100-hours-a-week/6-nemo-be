package kr.ai.nemo.exception.global;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
  private String errorCode;
  private String errorMessage;
  private int httpStatus;
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
