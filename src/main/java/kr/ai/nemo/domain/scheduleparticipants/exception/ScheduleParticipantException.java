package kr.ai.nemo.domain.scheduleparticipants.exception;

import lombok.Getter;

@Getter
public class ScheduleParticipantException extends RuntimeException {
  private final ScheduleParticipantErrorCode errorCode;

  public ScheduleParticipantException(ScheduleParticipantErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
