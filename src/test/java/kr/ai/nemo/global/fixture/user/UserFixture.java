package kr.ai.nemo.global.fixture.user;

import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;
import kr.ai.nemo.global.testUtil.TestReflectionUtils;

import java.time.LocalDateTime;

/**
 * 테스트용 User 픽스처 클래스
 */
public class UserFixture {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

    public static User createUser(String nickname, String email, String provider, String providerId) {

      return User.builder()
              .nickname(nickname)
              .email(email)
              .provider(provider)
              .providerId(providerId)
              .profileImageUrl("https://example.com/profile.jpg")
              .status(UserStatus.ACTIVE)
              .createdAt(FIXED_TIME)
              .updatedAt(FIXED_TIME)
              .build();
    }

    public static User createDefaultUser() {
        return createUser("testUser", "test@example.com", "kakao", "123456789");
    }
    
    public static User createUserWithId(Long id, String nickname) {
        User user = createUser(nickname, nickname + "@example.com", "kakao", String.valueOf(id));
        TestReflectionUtils.setField(user, "id", id);
        return user;
    }
}
