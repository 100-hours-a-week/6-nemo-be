package kr.ai.nemo.auth.dto;

public record KakaoTokenResponse (
  String tokenType,
  String accessToken,
  int expiresIn,
  String refreshToken,
  int refreshTokenExpiresIn,
  String scope
) {}

