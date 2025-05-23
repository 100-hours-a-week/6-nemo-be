package kr.ai.nemo.groupparticipants.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {
  LEADER("모임장"),
  MEMBER("모임원");

  private final String description;
}
