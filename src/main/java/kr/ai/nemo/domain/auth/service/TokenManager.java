package kr.ai.nemo.domain.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
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
  private final JwtProvider jwtProvider;
  private final UserTokenService userTokenService;

  public String createAccessToken(Long userId) {
    return jwtProvider.createAccessToken(userId);
  }

  public String createRefreshToken(Long userId) {
    return jwtProvider.createRefreshToken(userId);
  }

  public void saveOrUpdateToken(User user, String provider, String refreshToken,
      String device, LocalDateTime expiryDate) {
    userTokenService.saveOrUpdateToken(user, provider, refreshToken, device, expiryDate);
  }

  public void setRefreshTokenInCookie(HttpServletResponse response, String refreshToken) {
    Cookie cookie = new Cookie(AuthConstants.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge((int) (jwtProvider.getRefreshTokenValidity() / 1000)); // 초 단위
    response.addCookie(cookie);
  }
  @TimeTrace
  public String reissueAccessToken(String refreshToken) {
    UserToken userToken = userTokenService.findValidToken(refreshToken);
    User user = userToken.getUser();

    return jwtProvider.createAccessToken(user.getId());
  }

  public void clearRefreshTokenCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie(AuthConstants.REFRESH_TOKEN_COOKIE_NAME, null);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }
}
