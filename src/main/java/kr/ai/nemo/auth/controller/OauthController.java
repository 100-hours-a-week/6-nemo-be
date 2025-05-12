package kr.ai.nemo.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import kr.ai.nemo.auth.dto.TokenRefreshResponse;
import kr.ai.nemo.auth.exception.OAuthErrorCode;
import kr.ai.nemo.auth.exception.OAuthException;
import kr.ai.nemo.auth.service.OauthService;
import kr.ai.nemo.common.exception.ApiResponse;
import kr.ai.nemo.user.dto.UserLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OauthController {

  private final OauthService oauthService;

  @GetMapping("/auth/kakao/callback")
  public ResponseEntity<ApiResponse<UserLoginResponse>> kakaoLogin(
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "error", required = false) String error,
      @RequestParam(value = "error_description", required = false) String errorDescription,
      HttpServletResponse response) {

    if (error != null) {
      throw new OAuthException(OAuthErrorCode.KAKAO_AUTH_ERROR);
    }

    if (code == null || code.isEmpty()) {
      throw new OAuthException(OAuthErrorCode.CODE_MISSING);
    }

    return ResponseEntity.ok(ApiResponse.success(oauthService.loginWithKakao(code, response)));
  }

  @PostMapping("/api/v1/auth/token/refresh")
  public ResponseEntity<ApiResponse<TokenRefreshResponse>> tokenRefresh(
      @CookieValue(name = "refresh_token", required = true) String refreshToken
  ) {
    return ResponseEntity.ok(ApiResponse.success(oauthService.reissueAccessToken(refreshToken)));
  }
}
