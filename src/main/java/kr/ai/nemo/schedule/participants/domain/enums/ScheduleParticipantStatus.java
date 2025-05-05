package kr.ai.nemo.schedule.participants.domain.enums;

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
