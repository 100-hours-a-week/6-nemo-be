package kr.ai.nemo.domain.auth.exception;

import kr.ai.nemo.global.error.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum KakaoOAuthErrorCode implements ErrorCode {

  // 400 BAD REQUEST
  INVALID_CODE("INVALID_CODE", "유효하지 않은 인가 코드입니다.", HttpStatus.BAD_REQUEST),
  MISSING_USER_ID("MISSING_USER_ID", "카카오 사용자 ID가 없습니다.", HttpStatus.BAD_REQUEST),
  CODE_MISSING("CODE_MISSING", "인가코드가 없습니다.", HttpStatus.BAD_REQUEST),

  // 401 UNAUTHORIZED
  EMPTY_ACCESS_TOKEN("EMPTY_ACCESS_TOKEN", "카카오 액세스 토큰이 비어있습니다", HttpStatus.UNAUTHORIZED),
  INVALID_CLIENT("INVALID_CLIENT", "잘못된 클라이언트 정보입니다.", HttpStatus.UNAUTHORIZED),
  INVALID_ACCESS_TOKEN("INVALID_ACCESS_TOKEN", "유효하지 않은 액세스 토큰입니다.", HttpStatus.UNAUTHORIZED),
  KAKAO_AUTH_ERROR("KAKAO_AUTH_ERROR", "카카오 로그인을 취소했습니다.", HttpStatus.UNAUTHORIZED),

  // 500 INTERNAL SERVER ERROR
  EMPTY_TOKEN_RESPONSE("EMPTY_TOKEN_RESPONSE", "카카오 토큰 응답이 비어있습니다", HttpStatus.INTERNAL_SERVER_ERROR),
  EMPTY_USER_INFO("EMPTY_USER_INFO", "카카오에서 사용자 정보를 불러올 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  USER_INFO_ERROR("USER_INFO_ERROR", "카카오 사용자 정보 호출 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_USER_RESPONSE("INVALID_USER_RESPONSE", "유효하지 않은 카카오 사용자 정보입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  USER_PROCESSING_ERROR("USER_PROCESSING_ERROR", "사용자 정보 처리 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR),
  LOGIN_ERROR("LOGIN_ERROR", "카카오 로그인 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

  // 502 BAD GATEWAY
  CLIENT_ERROR("CLIENT_ERROR", "카카오 API 호출 중 클라이언트 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),

  // 503 SERVICE UNAVAILABLE
  CONNECTION_ERROR("CONNECTION_ERROR", "카카오 API 연결 실패", HttpStatus.SERVICE_UNAVAILABLE);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}

