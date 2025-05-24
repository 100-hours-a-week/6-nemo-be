package kr.ai.nemo.schedule.exception;

import lombok.Getter;

@Getter
public class ScheduleException extends RuntimeException {
  private final ScheduleErrorCode errorCode;

  public ScheduleException(ScheduleErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
