package kr.ai.nemo.auth.exception;

import lombok.Getter;

@Getter
public enum OAuthErrorCode {
  EMPTY_ACCESS_TOKEN("카카오 액세스 토큰이 비어있습니다"),
  EMPTY_TOKEN_RESPONSE("카카오 토큰 응답이 비어있습니다"),
  INVALID_CODE("유효하지 않은 인가 코드"),
  INVALID_CLIENT("잘못된 클라이언트 정보"),
  CLIENT_ERROR("카카오 API 호출 중 클라이언트 오류"),
  CONNECTION_ERROR("카카오 API 연결 실패"),
  EMPTY_USER_INFO("카카오에서 사용자 정보를 불러올 수 없습니다"),
  MISSING_USER_ID("카카오 사용자 ID가 없습니다"),
  INVALID_ACCESS_TOKEN("유효하지 않은 액세스 토큰"),
  USER_INFO_ERROR("카카오 사용자 정보 호출 중 오류"),
  INVALID_USER_RESPONSE("유효하지 않은 카카오 사용자 정보"),
  USER_PROCESSING_ERROR("사용자 정보 처리 중 오류 발생"),
  LOGIN_ERROR("카카오 로그인 중 오류가 발생했습니다"),
  KAKAO_AUTH_ERROR("카카오 로그인을 취소했습니다."),
  CODE_MISSING("인가코드가 없습니다.");

  private final String message;

  OAuthErrorCode(String message) {
    this.message = message;
  }
}
