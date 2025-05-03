package kr.ai.nemo.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
  private final ResponseCode responseCode;

  public CustomException(ResponseCode responseCode) {
    super(responseCode.getMessage());
    this.responseCode = responseCode;
  }
}