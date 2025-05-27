package kr.ai.nemo.domain.auth.dto;

public record KakaoTokenResponse (
  String tokenType,
  String accessToken,
  int expiresIn,
  String refreshToken,
  int refreshTokenExpiresIn,
  String scope
) {}

