package kr.ai.nemo.domain.auth.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Token {
  ACCESS_TOKEN("access_token"),
  REFRESH_TOKEN("refresh_token");

  private final String value;
}
