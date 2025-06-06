package kr.ai.nemo.domain.group.exception;


import kr.ai.nemo.global.error.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum GroupErrorCode implements ErrorCode {

  // 400 BAD REQUEST
  INVALID_CATEGORY("INVALID_CATEGOTY","올바르지 않은 모임 카테고리입니다.", HttpStatus.BAD_REQUEST ),

  // 403 Forbidden
  NOT_GROUP_OWNER("GROUP_KICK_FORBIDDEN", "추방 권한이 없습니다.", HttpStatus.FORBIDDEN),

  // 404 NOT FOUND
  GROUP_NOT_FOUND("GROUP_NOT_FOUND", "모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  // 409 Conflict
  GROUP_DISBANDED("GROUP_DISBANDED", "해체된 모임입니다.", HttpStatus.CONFLICT),
  ALREADY_APPLIED_OR_JOINED("ALREADY_APPLIED_OR_JOINED", "이미 신청했거나 참여중인 사용자입니다.", HttpStatus.CONFLICT),
  GROUP_FULL("GROUP_FULL", "모임 정원이 가득 찼습니다.", HttpStatus.CONFLICT);


  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
