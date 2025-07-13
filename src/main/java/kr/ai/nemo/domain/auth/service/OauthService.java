package kr.ai.nemo.domain.auth.service;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import kr.ai.nemo.global.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.domain.enums.LoginDevice;
import kr.ai.nemo.domain.auth.domain.enums.OAuthProvider;
import kr.ai.nemo.domain.auth.dto.KakaoTokenResponse;
import kr.ai.nemo.domain.auth.dto.KakaoUserResponse;
import kr.ai.nemo.domain.auth.exception.KakaoOAuthErrorCode;
import kr.ai.nemo.domain.auth.exception.AuthException;
import kr.ai.nemo.domain.user.domain.User;
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

  @TimeTrace
  @Transactional
  public String loginWithKakao(String code, String error, HttpServletResponse response) {
    if (error != null) {
      throw new AuthException(KakaoOAuthErrorCode.KAKAO_AUTH_ERROR);
    }

    if (code == null || code.isEmpty()) {
      throw new AuthException(KakaoOAuthErrorCode.CODE_MISSING);
    }
    try {
      KakaoTokenResponse kakaoToken = kakaoClient.getAccessToken(code);
      if (kakaoToken.accessToken() == null || kakaoToken.accessToken().isEmpty()) {
        throw new AuthException(KakaoOAuthErrorCode.EMPTY_ACCESS_TOKEN);
      }

      KakaoUserResponse userResponse = kakaoClient.getUserInfo(kakaoToken.accessToken());
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

  @TimeTrace
  public String reissueAccessToken(String refreshToken) {
    return tokenManager.reissueAccessToken(refreshToken);
  }

  @TimeTrace
  @Transactional
  public void invalidateRefreshToken(String refreshToken) {
    userTokenService.revokeToken(refreshToken);
  }
}
