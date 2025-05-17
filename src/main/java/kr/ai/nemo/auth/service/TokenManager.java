package kr.ai.nemo.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import kr.ai.nemo.auth.domain.UserToken;
import kr.ai.nemo.auth.dto.TokenRefreshResponse;
import kr.ai.nemo.auth.jwt.JwtProvider;
import kr.ai.nemo.common.constants.AuthConstants;
import kr.ai.nemo.user.domain.User;
import lombok.RequiredArgsConstructor;
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

  public TokenRefreshResponse reissueAccessToken(String refreshToken) {
    UserToken userToken = userTokenService.findValidToken(refreshToken);
    User user = userToken.getUser();

    String newAccessToken = jwtProvider.createAccessToken(user.getId());
    long expiresIn = jwtProvider.getAccessTokenValidity();

    return new TokenRefreshResponse(newAccessToken, expiresIn);
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
