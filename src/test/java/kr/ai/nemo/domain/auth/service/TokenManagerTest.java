package kr.ai.nemo.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import kr.ai.nemo.domain.auth.domain.UserToken;
import kr.ai.nemo.domain.auth.security.JwtProvider;
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
@DisplayName("TokenManager 테스트")
class TokenManagerTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserTokenService userTokenService;

    @InjectMocks
    private TokenManager tokenManager;

    @Test
    @DisplayName("[성공] 액세스 토큰 생성")
    void createAccessToken_Success() {
        // given
        Long userId = 1L;
        String expectedToken = "access-token";
        given(jwtProvider.createAccessToken(userId)).willReturn(expectedToken);

        // when
        String result = tokenManager.createAccessToken(userId);

        // then
        assertThat(result).isEqualTo(expectedToken);
        verify(jwtProvider).createAccessToken(userId);
    }

    @Test
    @DisplayName("[성공] 리프레시 토큰 생성")
    void createRefreshToken_Success() {
        // given
        Long userId = 1L;
        String expectedToken = "refresh-token";
        given(jwtProvider.createRefreshToken(userId)).willReturn(expectedToken);

        // when
        String result = tokenManager.createRefreshToken(userId);

        // then
        assertThat(result).isEqualTo(expectedToken);
        verify(jwtProvider).createRefreshToken(userId);
    }

    @Test
    @DisplayName("[성공] 토큰 저장 또는 업데이트")
    void saveOrUpdateToken_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        String provider = "kakao";
        String refreshToken = "refresh-token";
        String device = "WEB";
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(30);

        // when
        tokenManager.saveOrUpdateToken(user, provider, refreshToken, device, expiryDate);

        // then
        verify(userTokenService).saveOrUpdateToken(user, provider, refreshToken, device, expiryDate);
    }

    @Test
    @DisplayName("[성공] 리프레시 토큰 쿠키 설정")
    void setRefreshTokenInCookie_Success() {
        // given
        HttpServletResponse response = mock(HttpServletResponse.class);
        String refreshToken = "refresh-token";
        long validity = 86400000L; // 1일
        given(jwtProvider.getRefreshTokenValidity()).willReturn(validity);

        // when
        tokenManager.setRefreshTokenInCookie(response, refreshToken);

        // then
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        
        Cookie cookie = cookieCaptor.getValue();
        assertThat(cookie.getName()).isEqualTo("refresh_token");
        assertThat(cookie.getValue()).isEqualTo(refreshToken);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
    }

    @Test
    @DisplayName("[성공] 액세스 토큰 재발급")
    void reissueAccessToken_Success() {
        // given
        String refreshToken = "refresh-token";
        User user = UserFixture.createDefaultUser();
        UserToken userToken = UserToken.builder()
                .user(user)
                .refreshToken(refreshToken)
                .provider("kakao")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        String newAccessToken = "new-access-token";
        long validity = 3600000L; // 1시간

        given(userTokenService.findValidToken(refreshToken)).willReturn(userToken);
        given(jwtProvider.createAccessToken(user.getId())).willReturn(newAccessToken);
        given(jwtProvider.getAccessTokenValidity()).willReturn(validity);

        // when
        String result = tokenManager.reissueAccessToken(refreshToken);

        // then
        assertThat(result).isEqualTo(newAccessToken);
        verify(userTokenService).findValidToken(refreshToken);
        verify(jwtProvider).createAccessToken(user.getId());
        verify(jwtProvider).getAccessTokenValidity();
    }

    @Test
    @DisplayName("[성공] 리프레시 토큰 쿠키 삭제")
    void clearRefreshTokenCookie_Success() {
        // given
        HttpServletResponse response = mock(HttpServletResponse.class);

        // when
        tokenManager.clearRefreshTokenCookie(response);

        // then
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());

        Cookie cookie = cookieCaptor.getValue();
        assertThat(cookie.getName()).isEqualTo("refresh_token");
        assertThat(cookie.getValue()).isNull();
        assertThat(cookie.getMaxAge()).isZero();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
    }
}
