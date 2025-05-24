package kr.ai.nemo.groupparticipants.exception;

import kr.ai.nemo.global.error.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum GroupParticipantErrorCode implements ErrorCode {

  // 404 NOT FOUND
  NOT_GROUP_MEMBER("PARTICIPANT_NOT_FOUND", "모임원이 아닙니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
