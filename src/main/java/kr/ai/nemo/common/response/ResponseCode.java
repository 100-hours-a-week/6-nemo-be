package kr.ai.nemo.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCode {
  // 성공 응답
  SUCCESS(200, "Success"),
  CREATED(201, "Created"),
  ACCEPTED(202, "Accepted"),
  NO_CONTENT(204, "No Content"),
  
  // 리다이렉션
  MOVED_PERMANENTLY(301, "Moved Permanently"),
  FOUND(302, "Found"),
  SEE_OTHER(303, "See Other"),
  
  // 클라이언트 오류
  BAD_REQUEST(400, "Bad Request"),
  UNAUTHORIZED(401, "Unauthorized"),
  FORBIDDEN(403, "Forbidden"),
  NOT_FOUND(404, "Not Found"),
  METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
  CONFLICT(409, "Conflict"),
  
  // 서버 오류
  INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
  NOT_IMPLEMENTED(501, "Not Implemented"),
  BAD_GATEWAY(502, "Bad Gateway"),
  SERVICE_UNAVAILABLE(503, "Service Unavailable");

  private final int code;
  private final String message;
}
