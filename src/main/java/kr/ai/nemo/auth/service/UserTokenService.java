package kr.ai.nemo.auth.service;

import java.time.LocalDateTime;

import kr.ai.nemo.auth.domain.UserToken;
import kr.ai.nemo.auth.repository.UserTokenRepository;
import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import kr.ai.nemo.user.domain.User;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserTokenService {

  private final UserTokenRepository userTokenRepository;

  @Transactional
  public void saveOrUpdateToken(User user, String provider, String refreshToken,
      String deviceInfo, LocalDateTime expiresAt) {

    userTokenRepository.findByUserAndProvider(user, provider)
        .ifPresentOrElse(
            existingToken -> updateExistingToken(existingToken, refreshToken, deviceInfo, expiresAt),
            () -> createAndSaveRefreshToken(user, provider, refreshToken, deviceInfo, expiresAt)
        );
  }

  private void updateExistingToken(UserToken token, String refreshToken,
      String deviceInfo, LocalDateTime expiresAt) {
    token.setRefreshToken(refreshToken);
    token.setDeviceInfo(deviceInfo);
    token.setExpiresAt(expiresAt);
    token.setRevoked(false);
    token.setUpdatedAt(LocalDateTime.now());
    userTokenRepository.save(token);
  }

  private void createAndSaveRefreshToken(User user, String provider, String refreshToken,
      String deviceInfo, LocalDateTime expiresAt) {

    UserToken newToken = UserToken.builder()
        .user(user)
        .provider(provider)
        .refreshToken(refreshToken)
        .deviceInfo(deviceInfo)
        .revoked(false)
        .expiresAt(expiresAt)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();

    userTokenRepository.save(newToken);
  }

  public UserToken findValidToken(String refreshToken) {
    if (refreshToken.startsWith("Bearer ")) {
      refreshToken = refreshToken.substring(7);  // 꼭 제거해야 DB 값과 비교 가능
    }
    return userTokenRepository.findByRefreshTokenAndRevokedFalse(refreshToken)
        .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
        .orElseThrow(() -> new CustomException(ResponseCode.INVALID_TOKEN));
  }
}
