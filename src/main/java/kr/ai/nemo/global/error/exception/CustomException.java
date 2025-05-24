package kr.ai.nemo.global.error.exception;

import kr.ai.nemo.global.error.code.CommonErrorCode;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
  private final CommonErrorCode commonErrorCode;

  public CustomException(CommonErrorCode commonErrorCode) {
    super(commonErrorCode.getMessage());
    this.commonErrorCode = commonErrorCode;
  }
}
