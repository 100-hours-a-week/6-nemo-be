package kr.ai.nemo.domain.groupparticipants.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Status {
  PENDING("신청중"),
  JOINED("가입중"),
  KICKED("추방됨"),
  WITHDRAWN("탈퇴"),
  REJECTED("거절됨");

  private final String displayName;

  public boolean isJoined() {
    return this == JOINED;
  }
}
