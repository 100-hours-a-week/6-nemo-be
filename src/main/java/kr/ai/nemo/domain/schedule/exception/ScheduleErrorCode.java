package kr.ai.nemo.domain.schedule.exception;

import kr.ai.nemo.unit.global.error.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ScheduleErrorCode implements ErrorCode {

  // 403 FORBIDDEN
  SCHEDULE_DELETE_FORBIDDEN("SCHEDULE_DELETE_FORBIDDEN", "일정 생성자만 취소할 수 있습니다.", HttpStatus.FORBIDDEN),

  // 404 NOT FOUND
  SCHEDULE_NOT_FOUND("SCHEDULE_NOT_FOUND", "일정이 존재하지 않습니다.", HttpStatus.NOT_FOUND),

  // 409 Conflict
  SCHEDULE_ALREADY_CANCELED("SCHEDULE_ALREADY_CANCELED", "이미 취소된 일정입니다.", HttpStatus.CONFLICT),
  SCHEDULE_ALREADY_ENDED("SCHEDULE_ALREADY_ENDED", "이미 종료된 일정은 취소할 수 없습니다.", HttpStatus.CONFLICT),
  SCHEDULE_ALREADY_STARTED_OR_ENDED("SCHEDULE_ALREADY_STARTED_OR_ENDED", "이미 종료된 일정입니다." , HttpStatus.CONFLICT );

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
