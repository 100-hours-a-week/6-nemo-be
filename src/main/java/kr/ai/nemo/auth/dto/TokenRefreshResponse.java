package kr.ai.nemo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
public class TokenRefreshResponse {
  private String accessToken;
  private int accessTokenExpiresIn;
}
