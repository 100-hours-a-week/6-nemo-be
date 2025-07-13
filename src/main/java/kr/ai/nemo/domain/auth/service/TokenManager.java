
package kr.ai.nemo.domain.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import kr.ai.nemo.global.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.domain.UserToken;
import kr.ai.nemo.domain.auth.security.JwtProvider;
import kr.ai.nemo.global.util.AuthConstants;
import kr.ai.nemo.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenManager {

  private static final int MILLISECONDS_TO_SECONDS = 1000;
  private static final int COOKIE_MAX_AGE_EXPIRED = 0;
  private static final String SET_COOKIE_HEADER = "Set-Cookie";

  private final JwtProvider jwtProvider;
  private final UserTokenService userTokenService;

  // 토큰 생성 메서드들
  public String createAccessToken(Long userId) {
    return jwtProvider.createAccessToken(userId);
  }

  public String createRefreshToken(Long userId) {
    return jwtProvider.createRefreshToken(userId);
  }

  // 토큰 저장 메서드
  public void saveOrUpdateToken(User user, String provider, String refreshToken,
      String device, LocalDateTime expiryDate) {
    userTokenService.saveOrUpdateToken(user, provider, refreshToken, device, expiryDate);
  }

  // 토큰 재발급 메서드
  @TimeTrace
  public String reissueAccessToken(String refreshToken) {
    UserToken userToken = userTokenService.findValidToken(refreshToken);
    User user = userToken.getUser();

    return jwtProvider.createAccessToken(user.getId());
  }

  // 쿠키 설정 메서드들
  public void setRefreshTokenInCookie(HttpServletResponse response, String refreshToken) {
    int maxAge = (int) (jwtProvider.getRefreshTokenValidity() / MILLISECONDS_TO_SECONDS);
    ResponseCookie cookie = createCookie(AuthConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken, maxAge);
    response.addHeader(SET_COOKIE_HEADER, cookie.toString());
  }

  public void clearRefreshTokenCookie(HttpServletResponse response) {
    ResponseCookie cookie = createCookie(AuthConstants.REFRESH_TOKEN_COOKIE_NAME, "", COOKIE_MAX_AGE_EXPIRED);
    response.addHeader(SET_COOKIE_HEADER, cookie.toString());
  }

  public void clearAccessTokenCookie(HttpServletResponse response) {
    ResponseCookie cookie = createCookie(AuthConstants.ACCESS_TOKEN_COOKIE_NAME, "", COOKIE_MAX_AGE_EXPIRED);
    response.addHeader(SET_COOKIE_HEADER, cookie.toString());
  }

  // 공통 쿠키 생성 메서드
  private ResponseCookie createCookie(String name, String value, int maxAge) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(maxAge)
        .sameSite("None")
        .build();
  }
}
