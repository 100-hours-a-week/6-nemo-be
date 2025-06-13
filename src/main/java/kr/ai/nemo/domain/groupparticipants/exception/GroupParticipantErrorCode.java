package kr.ai.nemo.domain.groupparticipants.exception;

import kr.ai.nemo.global.error.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum GroupParticipantErrorCode implements ErrorCode {

  // 404 NOT FOUND
  NOT_GROUP_MEMBER("PARTICIPANT_NOT_FOUND", "모임원이 아닙니다.", HttpStatus.NOT_FOUND),

  // 409 Conflict
  ALREADY_KICKED_MEMBER("ALREADY_KICKED_MEMBER", "추방된 모임원입니다.", HttpStatus.CONFLICT),
  ALREADY_WITHDRAWN_MEMBER("ALREADY_WITHDRAWN_MEMBER", "탈퇴한 모임원입니다.", HttpStatus.CONFLICT),
  LEADER_CANNOT_BE_REMOVED("LEADER_CANNOT_BE_REMOVED", "모임장은 탈퇴하거나 추방할 수 없습니다.", HttpStatus.CONFLICT),;

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
