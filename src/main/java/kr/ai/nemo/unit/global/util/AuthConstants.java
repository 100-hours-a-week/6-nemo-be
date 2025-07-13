package kr.ai.nemo.unit.global.util;

public class AuthConstants {
  // 쿠키 관련
  public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
  public static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";

  // 헤더 관련
  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";

  // 메시지 관련
  public static final String LOGOUT_SUCCESS_MESSAGE = "로그아웃 성공";

  // 카카오 API 관련
  public static final String KAKAO_LOGOUT_URL = "https://kapi.kakao.com/v1/user/logout";

  private AuthConstants() {
    // 인스턴스화 방지
  }
}
