package kr.ai.nemo.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse (
    @JsonProperty("id")
    Long id,

    @JsonProperty("kakao_account")
    KakaoAccount kakaoAccount
) {
  public record KakaoAccount (
      @JsonProperty("email")
      String email,

      @JsonProperty("profile")
      Profile profile
  ) {}

  public record Profile (
      @JsonProperty("nickname")
      String nickname,

      @JsonProperty("profile_image_url")
      String profileImageUrl,

      @JsonProperty("is_default_image")
      boolean isDefaultImage
  ) {}
}
