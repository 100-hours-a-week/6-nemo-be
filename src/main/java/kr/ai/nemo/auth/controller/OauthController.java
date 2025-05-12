package kr.ai.nemo.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import kr.ai.nemo.auth.dto.TokenRefreshResponse;
import kr.ai.nemo.auth.service.OauthService;
import kr.ai.nemo.common.UriGenerator;
import kr.ai.nemo.common.exception.ApiResponse;
import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OauthController {

  private final OauthService oauthService;

  @GetMapping("/auth/kakao/callback")
  public void kakaoLogin(
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "error", required = false) String error,
      HttpServletResponse response) {
    oauthService.handleKakaoCallback(code, error, response);
    try {
      response.sendRedirect(UriGenerator.login().toString());
    } catch (IOException e) {
      throw new CustomException(ResponseCode.REDIRECT_FAIL);
    }
  }

  @PostMapping("/api/v1/auth/token/refresh")
  public ResponseEntity<ApiResponse<TokenRefreshResponse>> tokenRefresh(
      @CookieValue(name = "refresh_token", required = true) String refreshToken
  ) {
    return ResponseEntity.ok(ApiResponse.success(oauthService.reissueAccessToken(refreshToken)));
  }
}
