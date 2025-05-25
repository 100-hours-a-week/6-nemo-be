package kr.ai.nemo.domain.scheduleparticipants.domain.enums;

public enum ScheduleParticipantStatus {
  PENDING,
  ACCEPTED,
  REJECTED;

  public boolean isPending() {
    return this == PENDING;
  }

  public boolean isAccepted() {
    return this == ACCEPTED;
  }
}
