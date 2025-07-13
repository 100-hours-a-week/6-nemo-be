package kr.ai.nemo.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.ai.nemo.domain.auth.domain.UserToken;
import kr.ai.nemo.domain.auth.exception.AuthException;
import kr.ai.nemo.domain.auth.repository.UserTokenRepository;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserTokenService 테스트")
class UserTokenServiceTest {

    @Mock
    private UserTokenRepository userTokenRepository;

    @InjectMocks
    private UserTokenService userTokenService;

    @Test
    @DisplayName("[성공] 새로운 토큰 저장")
    void saveOrUpdateToken_CreateNew_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        String provider = "kakao";
        String refreshToken = "refresh-token";
        String deviceInfo = "WEB";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

        given(userTokenRepository.findByUserIdAndProvider(user.getId(), provider))
                .willReturn(Optional.empty());

        // when
        userTokenService.saveOrUpdateToken(user, provider, refreshToken, deviceInfo, expiresAt);

        // then
        ArgumentCaptor<UserToken> tokenCaptor = ArgumentCaptor.forClass(UserToken.class);
        verify(userTokenRepository).save(tokenCaptor.capture());

        UserToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getProvider()).isEqualTo(provider);
        assertThat(savedToken.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(savedToken.getDeviceInfo()).isEqualTo(deviceInfo);
        assertThat(savedToken.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(savedToken.isRevoked()).isFalse();
    }

    @Test
    @DisplayName("[성공] 기존 토큰 업데이트")
    void saveOrUpdateToken_UpdateExisting_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        String provider = "kakao";
        String refreshToken = "new-refresh-token";
        String deviceInfo = "WEB";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

        UserToken existingToken = UserToken.builder()
                .user(user)
                .provider(provider)
                .refreshToken("old-refresh-token")
                .deviceInfo("old-device")
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(15))
                .build();

        given(userTokenRepository.findByUserIdAndProvider(user.getId(), provider))
                .willReturn(Optional.of(existingToken));

        // when
        userTokenService.saveOrUpdateToken(user, provider, refreshToken, deviceInfo, expiresAt);

        // then
        verify(userTokenRepository).save(existingToken);
        assertThat(existingToken.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(existingToken.getDeviceInfo()).isEqualTo(deviceInfo);
        assertThat(existingToken.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(existingToken.isRevoked()).isFalse();
    }

    @Test
    @DisplayName("[성공] 토큰 무효화")
    void revokeToken_Success() {
        // given
        String refreshToken = "refresh-token";
        User user = UserFixture.createDefaultUser();
        UserToken userToken = UserToken.builder()
                .user(user)
                .refreshToken(refreshToken)
                .provider("kakao")
                .deviceInfo("WEB")
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        given(userTokenRepository.findByRefreshTokenAndRevokedFalse(refreshToken))
                .willReturn(Optional.of(userToken));

        // when
        userTokenService.revokeToken(refreshToken);

        // then
        verify(userTokenRepository).save(userToken);
        assertThat(userToken.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("[성공] 유효한 토큰 조회")
    void findValidToken_Success() {
        // given
        String refreshToken = "refresh-token";
        User user = UserFixture.createDefaultUser();
        UserToken userToken = UserToken.builder()
                .user(user)
                .refreshToken(refreshToken)
                .provider("kakao")
                .deviceInfo("WEB")
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        given(userTokenRepository.findByRefreshTokenAndRevokedFalse(refreshToken))
                .willReturn(Optional.of(userToken));

        // when
        UserToken result = userTokenService.findValidToken(refreshToken);

        // then
        assertThat(result).isEqualTo(userToken);
        verify(userTokenRepository).findByRefreshTokenAndRevokedFalse(refreshToken);
    }

    @Test
    @DisplayName("[성공] Bearer 프리픽스가 있는 토큰 조회")
    void findValidToken_WithBearerPrefix_Success() {
        // given
        String refreshToken = "refresh-token";
        String bearerToken = "Bearer " + refreshToken;
        User user = UserFixture.createDefaultUser();
        UserToken userToken = UserToken.builder()
                .user(user)
                .refreshToken(refreshToken)
                .provider("kakao")
                .deviceInfo("WEB")
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        given(userTokenRepository.findByRefreshTokenAndRevokedFalse(refreshToken))
                .willReturn(Optional.of(userToken));

        // when
        UserToken result = userTokenService.findValidToken(bearerToken);

        // then
        assertThat(result).isEqualTo(userToken);
        verify(userTokenRepository).findByRefreshTokenAndRevokedFalse(refreshToken);
    }

    @Test
    @DisplayName("[실패] 만료된 토큰 조회")
    void findValidToken_ExpiredToken_ThrowException() {
        // given
        String refreshToken = "expired-token";
        User user = UserFixture.createDefaultUser();
        UserToken expiredToken = UserToken.builder()
                .user(user)
                .refreshToken(refreshToken)
                .provider("kakao")
                .deviceInfo("WEB")
                .revoked(false)
                .expiresAt(LocalDateTime.now().minusDays(1)) // 만료됨
                .build();

        given(userTokenRepository.findByRefreshTokenAndRevokedFalse(refreshToken))
                .willReturn(Optional.of(expiredToken));

        // when & then
        assertThatThrownBy(() -> userTokenService.findValidToken(refreshToken))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 토큰 조회")
    void findValidToken_NotFound_ThrowException() {
        // given
        String refreshToken = "non-existent-token";
        given(userTokenRepository.findByRefreshTokenAndRevokedFalse(refreshToken))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userTokenService.findValidToken(refreshToken))
                .isInstanceOf(AuthException.class);
    }
}
