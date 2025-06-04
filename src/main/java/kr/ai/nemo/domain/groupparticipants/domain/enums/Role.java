package kr.ai.nemo.domain.groupparticipants.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {
  LEADER("모임장"),
  MEMBER("모임원"),
  NON_MEMBER("일반 사용자");

  private final String description;
}
