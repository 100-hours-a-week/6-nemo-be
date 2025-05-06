package kr.ai.nemo.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ResponseCode {

  SUCCESS("SUCCESS", "요청이 성공적으로 처리되었습니다.", HttpStatus.OK),
  CREATED("CREATED", "리소스가 성공적으로 생성되었습니다.", HttpStatus.CREATED),
  NO_CONTENT("NO_CONTENT", "응답할 내용이 없습니다.", HttpStatus.NO_CONTENT),

  // 400 BAD REQUEST
  INVALID_REQUEST("INVALID_REQUEST", "입력값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
  INVALID_ENUM("INVALID_ENUM", "올바르지 않은 타입입니다.", HttpStatus.BAD_REQUEST),
  MISSING_REQUIRED_PARAMETER("MISSING_REQUIRED_PARAMETER", "필수 요청 파라미터가 누락되었습니다.", HttpStatus.BAD_REQUEST),


  // 401 UNAUTHORIZED
  UNAUTHORIZED("UNAUTHORIZED", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
  INVALID_TOKEN("INVALID_TOKEN", "토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
  EXPIRED_TOKEN("EXPIRED_TOKEN", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),

  // 403 FORBIDDEN
  ACCESS_DENIED("NO_PERMISSION", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
  SCHEDULE_DELETE_FORBIDDEN("SCHEDULE_DELETE_FORBIDDEN", "일정 생성자만 취소할 수 있습니다.", HttpStatus.FORBIDDEN),


  // 404 NOT FOUND
  USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  GROUP_NOT_FOUND("GROUP_NOT_FOUND", "모임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  NOT_GROUP_MEMBER("PARTICIPANT+NOT_FOUND", "모임원이 아닙니다.", HttpStatus.NOT_FOUND),
  SCHEDULE_NOT_FOUND("SCHEDULE_NOT_FOUND", "일정이 존재하지 않습니다.", HttpStatus.NOT_FOUND),

  // 409 Conflict
  GROUP_DISBANDED("GROUP_DISBANDED", "해체된 모임입니다.", HttpStatus.CONFLICT),
  USER_WITHDRAWN("USER_WITHDRAWN", "탈퇴한 사용자입니다.", HttpStatus.CONFLICT),
  ALREADY_APPLIED_OR_JOINED("ALREADY_APPLIED_OR_JOINED", "이미 신청했거나 참여 중인 사용자입니다.", HttpStatus.CONFLICT),
  SCHEDULE_ALREADY_DECIDED("SCHEDULE_ALREADY_DECIDED", "이미 응답하셨습니다.", HttpStatus.CONFLICT),
  SCHEDULE_ALREADY_CANCELED("SCHEDULE_ALREADY_CANCELED", "이미 취소된 일정입니다.", HttpStatus.CONFLICT),
  SCHEDULE_ALREADY_ENDED("SCHEDULE_ALREADY_ENDED", "이미 종료된 일정은 취소할 수 없습니다.", HttpStatus.CONFLICT),

  // 500 INTERNAL SERVER ERROR
  INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  AI_SERVER_CONNECTION_FAILED("AI_SERVER_CONNECTION_FAILED", "AI 서버 연결에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  EXTERNAL_API_ERROR("EXTERNAL_API_ERROR", "외부 API 호출 중 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE);



  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
