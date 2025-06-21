package kr.ai.nemo.domain.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User 도메인 테스트")
class UserTest {

    @Test
    @DisplayName("[성공] User 생성")
    void createUser_Success() {
        // given
        String provider = "kakao";
        String providerId = "123456789";
        String email = "test@example.com";
        String nickname = "testUser";
        String profileImageUrl = "https://example.com/image.jpg";
        UserStatus status = UserStatus.ACTIVE;
        LocalDateTime now = LocalDateTime.now();

        // when
        User user = User.builder()
                .provider(provider)
                .providerId(providerId)
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .status(status)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // then
        assertThat(user.getProvider()).isEqualTo(provider);
        assertThat(user.getProviderId()).isEqualTo(providerId);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getProfileImageUrl()).isEqualTo(profileImageUrl);
        assertThat(user.getStatus()).isEqualTo(status);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("[성공] 닉네임 변경")
    void setNickname_Success() {
        // given
        User user = User.builder()
                .nickname("oldNickname")
                .email("test@example.com")
                .provider("kakao")
                .providerId("123")
                .status(UserStatus.ACTIVE)
                .build();

        String newNickname = "newNickname";

        // when
        user.setNickname(newNickname);

        // then
        assertThat(user.getNickname()).isEqualTo(newNickname);
    }

    @Test
    @DisplayName("[성공] 프로필 이미지 URL 변경")
    void setProfileImageUrl_Success() {
        // given
        User user = User.builder()
                .nickname("testUser")
                .email("test@example.com")
                .provider("kakao")
                .providerId("123")
                .profileImageUrl("old-image-url")
                .status(UserStatus.ACTIVE)
                .build();

        String newImageUrl = "new-image-url";

        // when
        user.setProfileImageUrl(newImageUrl);

        // then
        assertThat(user.getProfileImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    @DisplayName("[성공] 빌더 기본값 확인")
    void builderDefaults_Success() {
        // when
        User user = User.builder()
                .nickname("testUser")
                .email("test@example.com")
                .provider("kakao")
                .providerId("123")
                .status(UserStatus.ACTIVE)
                .build();

        // then
        assertThat(user.getOwnedGroups()).isNotNull().isEmpty();
        assertThat(user.getGroupParticipants()).isNotNull().isEmpty();
        assertThat(user.getSchedule()).isNotNull().isEmpty();
        assertThat(user.getScheduleParticipants()).isNotNull().isEmpty();
    }
}
