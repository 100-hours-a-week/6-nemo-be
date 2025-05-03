package kr.ai.nemo.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessResponseCode {
  SUCCESS("200", "Success");

  private final String code;
  private final String message;
}
