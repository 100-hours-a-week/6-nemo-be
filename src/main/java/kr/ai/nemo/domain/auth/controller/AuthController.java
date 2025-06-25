package kr.ai.nemo.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import kr.ai.nemo.domain.auth.domain.enums.Token;
import kr.ai.nemo.global.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.service.OauthService;
import kr.ai.nemo.domain.auth.service.TokenManager;
import kr.ai.nemo.global.common.SuccessCode;
import kr.ai.nemo.global.error.code.CommonErrorCode;
import kr.ai.nemo.global.util.UriGenerator;
import kr.ai.nemo.global.common.BaseApiResponse;
import kr.ai.nemo.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

  private final OauthService oauthService;
  private final UriGenerator uriGenerator;
  private final TokenManager tokenManager;

  @GetMapping("/api/v1/auth/login/kakao")
  public void kakaoLogin(
      HttpServletResponse response,
      @RequestParam("redirect_uri") String frontRedirectUri) {

    try {
      URI kakaoUri = uriGenerator.kakaoLogin(frontRedirectUri);
      response.sendRedirect(kakaoUri.toString());
    } catch (IOException e) {
      throw new CustomException(CommonErrorCode.REDIRECT_FAIL);
    }
  }

  @TimeTrace
  @GetMapping("/auth/kakao/callback")
  public void login(
      @RequestParam(value = "state", required = false) String state,
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "error", required = false) String error,
      HttpServletResponse response) {
    String accessToken = oauthService.loginWithKakao(code, error, response);
    try {
      ResponseCookie accessTokenCookie = ResponseCookie.from(Token.ACCESS_TOKEN.name(), accessToken)
          .httpOnly(true)
          .secure(true)
          .path("/")
          .maxAge(Duration.ofHours(1))
          .sameSite("None")
          .build();
      response.addHeader("Set-Cookie", accessTokenCookie.toString());
      response.sendRedirect(uriGenerator.login(state).toString());
    } catch (IOException e) {
      throw new CustomException(CommonErrorCode.REDIRECT_FAIL);
    }
  }

  @TimeTrace
  @PostMapping("/api/v1/auth/token/refresh")
  public ResponseEntity<BaseApiResponse<?>> tokenRefresh(
      @CookieValue(name = "refresh_token", required = true) String refreshToken,
      HttpServletResponse response
  ) {
    String newAccessToken = oauthService.reissueAccessToken(refreshToken);
    ResponseCookie cookie = ResponseCookie.from(Token.ACCESS_TOKEN.getValue(), newAccessToken)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(Duration.ofMinutes(60))
        .sameSite("None")
        .build();

    response.addHeader("Set-Cookie", cookie.toString());
    return ResponseEntity.noContent().build();
  }

  @TimeTrace
  @DeleteMapping("/api/v1/auth/logout/kakao")
  public ResponseEntity<BaseApiResponse<?>> logout(
      @CookieValue(name = "refresh_token", required = false) String refreshToken,
      HttpServletResponse response
  ) {
    if (refreshToken != null && !refreshToken.isEmpty()) {
      oauthService.invalidateRefreshToken(refreshToken);
    }

    tokenManager.clearRefreshTokenCookie(response);

    return ResponseEntity.ok(BaseApiResponse.success(SuccessCode.SUCCESS));
  }
}

