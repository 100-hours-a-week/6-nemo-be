package kr.ai.nemo.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class TokenRefreshResponse {
  private String accessToken;
  private long accessTokenExpiresIn;
}
