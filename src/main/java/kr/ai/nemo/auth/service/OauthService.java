package kr.ai.nemo.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import kr.ai.nemo.auth.domain.enums.LoginDevice;
import kr.ai.nemo.auth.domain.enums.OAuthProvider;
import kr.ai.nemo.auth.dto.KakaoTokenResponse;
import kr.ai.nemo.auth.dto.KakaoUserResponse;
import kr.ai.nemo.auth.dto.TokenRefreshResponse;
import kr.ai.nemo.auth.exception.OAuthErrorCode;
import kr.ai.nemo.auth.exception.OAuthException;
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

  @Transactional
  public String loginWithKakao(String code, HttpServletResponse response) {
    try {
      KakaoTokenResponse kakaoToken = kakaoClient.getAccessToken(code);
      if (kakaoToken.getAccessToken() == null || kakaoToken.getAccessToken().isEmpty()) {
        throw new OAuthException(OAuthErrorCode.EMPTY_ACCESS_TOKEN);
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

    } catch (OAuthException e) {
      throw e;
    } catch (Exception e) {
      throw new OAuthException(OAuthErrorCode.LOGIN_ERROR, e);
    }
  }

  public TokenRefreshResponse reissueAccessToken(String refreshToken) {
    return tokenManager.reissueAccessToken(refreshToken);
  }

  public String handleKakaoCallback(String code, String error, HttpServletResponse response) {
    if (error != null) {
      throw new OAuthException(OAuthErrorCode.KAKAO_AUTH_ERROR);
    }

    if (code == null || code.isEmpty()) {
      throw new OAuthException(OAuthErrorCode.CODE_MISSING);
    }

    return loginWithKakao(code, response);
  }

}
