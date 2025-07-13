package kr.ai.nemo.unit.global.fixture.auth;

import java.time.LocalDateTime;
import kr.ai.nemo.domain.auth.domain.UserToken;
import kr.ai.nemo.domain.user.domain.User;

/**
 * 테스트용 UserToken 픽스처 클래스
 */
public class UserTokenFixture {

    public static UserToken createUserToken(User user, String provider, String refreshToken) {
        return UserToken.builder()
                .user(user)
                .provider(provider)
                .refreshToken(refreshToken)
                .deviceInfo("WEB")
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static UserToken createDefaultUserToken(User user) {
        return createUserToken(user, "KAKAO", "default-refresh-token");
    }

    public static UserToken createExpiredUserToken(User user) {
        return UserToken.builder()
                .user(user)
                .provider("KAKAO")
                .refreshToken("expired-refresh-token")
                .deviceInfo("WEB")
                .revoked(false)
                .expiresAt(LocalDateTime.now().minusDays(1)) // 만료됨
                .createdAt(LocalDateTime.now().minusDays(31))
                .updatedAt(LocalDateTime.now().minusDays(31))
                .build();
    }

    public static UserToken createRevokedUserToken(User user) {
        return UserToken.builder()
                .user(user)
                .provider("KAKAO")
                .refreshToken("revoked-refresh-token")
                .deviceInfo("WEB")
                .revoked(true) // 무효화됨
                .expiresAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
