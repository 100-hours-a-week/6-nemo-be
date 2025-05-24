package kr.ai.nemo.scheduleparticipants.exception;

import kr.ai.nemo.global.error.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ScheduleParticipantErrorCode implements ErrorCode {

  // 403 Forbidden
  NOT_GROUP_MEMBER("NOT_GROUP_MEMBER", "모임원이 아니므로 일정에 참여할 수 없습니다.", HttpStatus.FORBIDDEN),

  // 409 Conflict
  SCHEDULE_ALREADY_DECIDED("SCHEDULE_ALREADY_DECIDED", "이미 응답하셨습니다.", HttpStatus.CONFLICT);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
