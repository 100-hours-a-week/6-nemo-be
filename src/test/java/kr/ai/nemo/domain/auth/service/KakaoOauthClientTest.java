package kr.ai.nemo.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import kr.ai.nemo.domain.auth.dto.KakaoTokenResponse;
import kr.ai.nemo.domain.auth.dto.KakaoUserResponse;
import kr.ai.nemo.domain.auth.exception.AuthException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("KakaoOauthClient 테스트")
class KakaoOauthClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private KakaoOauthClient kakaoOauthClient;

    @Test
    void setUp() {
        ReflectionTestUtils.setField(kakaoOauthClient, "restApiKey", "test-api-key");
        ReflectionTestUtils.setField(kakaoOauthClient, "redirectUri", "http://localhost:3000/auth/callback");
    }

    @Test
    @DisplayName("[성공] 액세스 토큰 요청")
    void getAccessToken_Success() {
        setUp();
        // given
        String code = "authorization_code";
        KakaoTokenResponse expectedResponse = new KakaoTokenResponse(
                "bearer", "access_token", 3600, "refresh_token", 2592000, "account_email profile"
        );
        ResponseEntity<KakaoTokenResponse> responseEntity = ResponseEntity.ok(expectedResponse);

        given(restTemplate.postForEntity(
                eq("https://kauth.kakao.com/oauth/token"),
                any(HttpEntity.class),
                eq(KakaoTokenResponse.class)
        )).willReturn(responseEntity);

        // when
        KakaoTokenResponse result = kakaoOauthClient.getAccessToken(code);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.accessToken()).isEqualTo("access_token");
        assertThat(result.tokenType()).isEqualTo("bearer");
        assertThat(result.expiresIn()).isEqualTo(3600);
        assertThat(result.refreshToken()).isEqualTo("refresh_token");
        assertThat(result.refreshTokenExpiresIn()).isEqualTo(2592000);
    }

    @Test
    @DisplayName("[실패] 액세스 토큰 요청 - null 응답")
    void getAccessToken_NullResponse_ThrowException() {
        setUp();
        // given
        String code = "authorization_code";
        ResponseEntity<KakaoTokenResponse> responseEntity = ResponseEntity.ok(null);

        given(restTemplate.postForEntity(
                eq("https://kauth.kakao.com/oauth/token"),
                any(HttpEntity.class),
                eq(KakaoTokenResponse.class)
        )).willReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> kakaoOauthClient.getAccessToken(code))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("[실패] 액세스 토큰 요청 - 연결 오류")
    void getAccessToken_ConnectionError_ThrowException() {
        setUp();
        // given
        String code = "authorization_code";

        given(restTemplate.postForEntity(
                eq("https://kauth.kakao.com/oauth/token"),
                any(HttpEntity.class),
                eq(KakaoTokenResponse.class)
        )).willThrow(new RestClientException("Connection failed"));

        // when & then
        assertThatThrownBy(() -> kakaoOauthClient.getAccessToken(code))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("[성공] 사용자 정보 요청")
    void getUserInfo_Success() {
        // given
        String accessToken = "valid_access_token";
        KakaoUserResponse expectedResponse = new KakaoUserResponse(
                123456789L,
                new KakaoUserResponse.KakaoAccount(
                        "test@example.com",
                        new KakaoUserResponse.Profile("testUser", "https://example.com/image.jpg", false)
                )
        );
        ResponseEntity<KakaoUserResponse> responseEntity = ResponseEntity.ok(expectedResponse);

        given(restTemplate.exchange(
                eq("https://kapi.kakao.com/v2/user/me"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoUserResponse.class)
        )).willReturn(responseEntity);

        // when
        KakaoUserResponse result = kakaoOauthClient.getUserInfo(accessToken);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        assertThat(result.id()).isEqualTo(123456789L);
        assertThat(result.kakaoAccount().email()).isEqualTo("test@example.com");
        assertThat(result.kakaoAccount().profile().nickname()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("[실패] 사용자 정보 요청 - null 응답")
    void getUserInfo_NullResponse_ThrowException() {
        // given
        String accessToken = "valid_access_token";
        ResponseEntity<KakaoUserResponse> responseEntity = ResponseEntity.ok(null);

        given(restTemplate.exchange(
                eq("https://kapi.kakao.com/v2/user/me"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoUserResponse.class)
        )).willReturn(responseEntity);

        // when & then
        assertThatThrownBy(() -> kakaoOauthClient.getUserInfo(accessToken))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("[실패] 사용자 정보 요청 - 연결 오류")
    void getUserInfo_ConnectionError_ThrowException() {
        // given
        String accessToken = "valid_access_token";

        given(restTemplate.exchange(
                eq("https://kapi.kakao.com/v2/user/me"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(KakaoUserResponse.class)
        )).willThrow(new RestClientException("Connection failed"));

        // when & then
        assertThatThrownBy(() -> kakaoOauthClient.getUserInfo(accessToken))
                .isInstanceOf(AuthException.class);
    }
}
