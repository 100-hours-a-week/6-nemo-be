package kr.ai.nemo.global.fixture.user;

import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * 테스트용 User 픽스처 클래스
 */
public class UserFixture {

    public static User createUser(String nickname, String email, String provider, String providerId) {
        return User.builder()
                .id(1L)
                .nickname(nickname)
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .profileImageUrl("https://example.com/profile.jpg")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static User createDefaultUser() {
        return createUser("testUser", "test@example.com", "kakao", "123456789");
    }
}
