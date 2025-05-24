package kr.ai.nemo.scheduleparticipants.domain.enums;

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
