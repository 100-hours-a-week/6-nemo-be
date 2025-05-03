package kr.ai.nemo.group.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GroupStatus {
  ACTIVE("활성"),
  DISBANDED("비활성");

  private final String displayName;
}