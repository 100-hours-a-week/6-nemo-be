package kr.ai.nemo.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResponse {

  private String accessToken;
  private String refreshToken;
  private int refreshTokenExpiresIn;
  private UserDto user;
}
