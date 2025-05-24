package kr.ai.nemo.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import kr.ai.nemo.auth.domain.enums.LoginDevice;
import kr.ai.nemo.auth.domain.enums.OAuthProvider;
import kr.ai.nemo.auth.dto.KakaoTokenResponse;
import kr.ai.nemo.auth.dto.KakaoUserResponse;
import kr.ai.nemo.auth.dto.TokenRefreshResponse;
import kr.ai.nemo.auth.exception.KakaoOAuthErrorCode;
import kr.ai.nemo.auth.exception.AuthException;
import kr.ai.nemo.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OauthService {
  private final KakaoOauthClient kakaoClient;
  private final OauthUserService userService;
  private final TokenManager tokenManager;
  private final UserTokenService userTokenService;

  @Transactional
  public String loginWithKakao(String code, HttpServletResponse response) {
    try {
      KakaoTokenResponse kakaoToken = kakaoClient.getAccessToken(code);
      if (kakaoToken.getAccessToken() == null || kakaoToken.getAccessToken().isEmpty()) {
        throw new AuthException(KakaoOAuthErrorCode.EMPTY_ACCESS_TOKEN);
      }

      KakaoUserResponse userResponse = kakaoClient.getUserInfo(kakaoToken.getAccessToken());
      User user = userService.handleUser(userResponse);

      String accessToken = tokenManager.createAccessToken(user.getId());
      String refreshToken = tokenManager.createRefreshToken(user.getId());

      tokenManager.saveOrUpdateToken(
          user,
          OAuthProvider.KAKAO.name(),
          refreshToken,
          LoginDevice.WEB.name(),
          LocalDateTime.now().plusDays(30)
      );

      tokenManager.setRefreshTokenInCookie(response, refreshToken);

      return accessToken;

    } catch (AuthException e) {
      throw e;
    } catch (Exception e) {
      throw new AuthException(KakaoOAuthErrorCode.LOGIN_ERROR);
    }
  }

  public TokenRefreshResponse reissueAccessToken(String refreshToken) {
    return tokenManager.reissueAccessToken(refreshToken);
  }

  public String handleKakaoCallback(String code, String error, HttpServletResponse response) {
    if (error != null) {
      throw new AuthException(KakaoOAuthErrorCode.KAKAO_AUTH_ERROR);
    }

    if (code == null || code.isEmpty()) {
      throw new AuthException(KakaoOAuthErrorCode.CODE_MISSING);
    }

    return loginWithKakao(code, response);
  }

  @Transactional
  public void invalidateRefreshToken(String refreshToken) {
    userTokenService.revokeToken(refreshToken);
  }
}
