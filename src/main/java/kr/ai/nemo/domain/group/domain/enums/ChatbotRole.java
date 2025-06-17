package kr.ai.nemo.domain.group.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatbotRole {
  USER("user"),
  AI("ai");

  private final String role;
}
