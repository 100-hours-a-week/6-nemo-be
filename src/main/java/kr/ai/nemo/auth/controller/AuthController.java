package kr.ai.nemo.auth.controller;

import kr.ai.nemo.auth.dto.KakaoTokenResponse;
import kr.ai.nemo.auth.dto.KakaoUserResponse;
import kr.ai.nemo.auth.service.OauthService;
import kr.ai.nemo.common.exception.ApiResponse;
import kr.ai.nemo.user.dto.UserDto;
import kr.ai.nemo.user.dto.UserLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class AuthController {

  private final OauthService oauthService;

  @GetMapping("/kakao/callback")
  public ResponseEntity<ApiResponse<UserLoginResponse>> kakaoLogin(@RequestParam("code") String code) {
    KakaoTokenResponse tokenResponse = oauthService.getAccessToken(code);
    KakaoUserResponse userResponse = oauthService.getUserInfo(tokenResponse.getAccessToken());

    UserLoginResponse response = UserLoginResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .refreshTokenExpiresIn(1209600)
        .user(UserDto.from(user))
        .build();

    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
