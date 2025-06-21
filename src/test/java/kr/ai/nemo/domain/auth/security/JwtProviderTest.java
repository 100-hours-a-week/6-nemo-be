package kr.ai.nemo.domain.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "secretKeyString", "test-secret-key-for-jwt-testing-purposes");
        ReflectionTestUtils.setField(jwtProvider, "accessTokenValidity", 1800000L); // 30분
        ReflectionTestUtils.setField(jwtProvider, "refreshTokenValidity", 1209600000L); // 2주
        jwtProvider.init();
    }

    @Test
    @DisplayName("액세스 토큰 생성 테스트")
    void createAccessToken() {
        // given
        Long userId = 1L;

        // when
        String accessToken = jwtProvider.createAccessToken(userId);

        // then
        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isNotEmpty();
    }

    @Test
    @DisplayName("리프레시 토큰 생성 테스트")
    void createRefreshToken() {
        // given
        Long userId = 1L;

        // when
        String refreshToken = jwtProvider.createRefreshToken(userId);

        // then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
    }

    @Test
    @DisplayName("유효한 토큰 검증 테스트")
    void validateToken_Valid() {
        // given
        Long userId = 1L;
        String token = jwtProvider.createAccessToken(userId);

        // when
        boolean isValid = jwtProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 토큰 검증 테스트")
    void validateToken_Invalid() {
        // given
        String invalidToken = "invalid.token.string";

        // when
        boolean isValid = jwtProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출 테스트")
    void getUserIdFromToken() {
        // given
        Long userId = 123L;
        String token = jwtProvider.createAccessToken(userId);

        // when
        Long extractedUserId = jwtProvider.getUserIdFromToken(token);

        // then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("getter 메서드 테스트")
    void getterTest() {
        // when & then
        assertThat(jwtProvider.getAccessTokenValidity()).isEqualTo(1800000L);
        assertThat(jwtProvider.getRefreshTokenValidity()).isEqualTo(1209600000L);
    }
}
