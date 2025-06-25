package kr.ai.nemo.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.HttpServletResponse;
import kr.ai.nemo.domain.auth.dto.KakaoTokenResponse;
import kr.ai.nemo.domain.auth.dto.KakaoUserResponse;
import kr.ai.nemo.domain.auth.dto.TokenRefreshResponse;
import kr.ai.nemo.domain.auth.exception.AuthException;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OauthService 테스트")
class OauthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KakaoOauthClient kakaoClient;

    @Mock
    private OauthUserService userService;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private UserTokenService userTokenService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private OauthService oauthService;

    @Test
    @DisplayName("[성공] 카카오 로그인 전체 플로우")
    void loginWithKakao_FullFlow_Success() {
        // given
        String code = "valid-authorization-code";
        String accessToken = "kakao-access-token";
        String refreshToken = "refresh-token";
        String jwtAccessToken = "jwt-access-token";
        
        KakaoTokenResponse tokenResponse = new KakaoTokenResponse("bearer", accessToken, 3600, refreshToken, 2592000, "account_email profile");
        KakaoUserResponse userResponse = new KakaoUserResponse(
                123456789L,
                new KakaoUserResponse.KakaoAccount(
                        "test@example.com",
                        new KakaoUserResponse.Profile("testUser", "https://example.com/image.jpg", false)
                )
        );
        User user = UserFixture.createDefaultUser();

        given(kakaoClient.getAccessToken(code)).willReturn(tokenResponse);
        given(kakaoClient.getUserInfo(accessToken)).willReturn(userResponse);
        given(userService.handleUser(userResponse)).willReturn(user);
        given(tokenManager.createAccessToken(user.getId())).willReturn(jwtAccessToken);
        given(tokenManager.createRefreshToken(user.getId())).willReturn(refreshToken);

        // when
        String result = oauthService.loginWithKakao(code, null, response);

        // then
        assertThat(result).isEqualTo(jwtAccessToken);
        verify(kakaoClient).getAccessToken(code);
        verify(kakaoClient).getUserInfo(accessToken);
        verify(userService).handleUser(userResponse);
        verify(tokenManager).createAccessToken(user.getId());
        verify(tokenManager).createRefreshToken(user.getId());
        verify(tokenManager).saveOrUpdateToken(any(), anyString(), anyString(), anyString(), any());
        verify(tokenManager).setRefreshTokenInCookie(response, refreshToken);
    }

    @Test
    @DisplayName("[실패] 카카오 로그인 - 에러 파라미터 존재")
    void loginWithKakao_WithError_ThrowException() {
        // given
        String code = "valid-code";
        String error = "access_denied";

        // when & then
        assertThatThrownBy(() -> oauthService.loginWithKakao(code, error, response))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("[실패] 카카오 로그인 - 코드 없음")
    void loginWithKakao_NoCode_ThrowException() {
        // when & then
        assertThatThrownBy(() -> oauthService.loginWithKakao(null, null, response))
                .isInstanceOf(AuthException.class);
        
        assertThatThrownBy(() -> oauthService.loginWithKakao("", null, response))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("[실패] 카카오 로그인 - 빈 액세스 토큰")
    void loginWithKakao_EmptyAccessToken_ThrowException() {
        // given
        String code = "valid-code";
        KakaoTokenResponse tokenResponse = new KakaoTokenResponse("bearer ", "", 3600, "refresh", 2592000, "account_email profile");

        given(kakaoClient.getAccessToken(code)).willReturn(tokenResponse);

        // when & then
        assertThatThrownBy(() -> oauthService.loginWithKakao(code, null, response))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("[실패] 카카오 로그인 - null 액세스 토큰")
    void loginWithKakao_NullAccessToken_ThrowException() {
        // given
        String code = "valid-code";
        KakaoTokenResponse tokenResponse = new KakaoTokenResponse("bearer ", null, 3600, "refresh", 2592000, "account_email profile");

        given(kakaoClient.getAccessToken(code)).willReturn(tokenResponse);

        // when & then
        assertThatThrownBy(() -> oauthService.loginWithKakao(code, null, response))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("[실패] 카카오 로그인 - 예상치 못한 예외")
    void loginWithKakao_UnexpectedException_ThrowException() {
        // given
        String code = "valid-code";
        given(kakaoClient.getAccessToken(code)).willThrow(new RuntimeException("Unexpected error"));

        // when & then
        assertThatThrownBy(() -> oauthService.loginWithKakao(code, null, response))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("[성공] 액세스 토큰 재발급")
    void reissueAccessToken_Success() {
        // given
        String refreshToken = "valid-refresh-token";
        TokenRefreshResponse expectedResponse = new TokenRefreshResponse("new-access-token", 3600000L);

        given(tokenManager.reissueAccessToken(refreshToken)).willReturn(expectedResponse);

        // when
        TokenRefreshResponse result = oauthService.reissueAccessToken(refreshToken);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(tokenManager).reissueAccessToken(refreshToken);
    }

    @Test
    @DisplayName("[성공] 리프레시 토큰 무효화")
    void invalidateRefreshToken_Success() {
        // given
        String refreshToken = "valid-refresh-token";

        // when
        oauthService.invalidateRefreshToken(refreshToken);

        // then
        verify(userTokenService).revokeToken(refreshToken);
    }
}
