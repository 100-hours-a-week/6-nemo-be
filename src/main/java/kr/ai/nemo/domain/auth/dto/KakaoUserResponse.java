package kr.ai.nemo.domain.auth.dto;

public record KakaoUserResponse (
  Long id,
  KakaoAccount kakaoAccount
) {
  public record KakaoAccount (
    String email,
    Profile profile
  ) {}
  public record Profile (
    String nickname,
    String profileImageUrl,
    boolean isDefaultImage
  ) {}
}
