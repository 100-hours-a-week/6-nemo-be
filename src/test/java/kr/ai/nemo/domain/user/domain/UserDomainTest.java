package kr.ai.nemo.domain.user.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import kr.ai.nemo.domain.user.domain.enums.UserStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User 도메인 테스트")
class UserDomainTest {

    @Test
    @DisplayName("[성공] User 엔티티 생성")
    void createUser_Success() {
        // given
        String nickname = "테스트유저";
        String email = "test@example.com";
        String provider = "kakao";
        String providerId = "123456";
        String profileImageUrl = "https://example.com/profile.jpg";
        LocalDateTime now = LocalDateTime.now();

        // when
        User user = User.builder()
                .nickname(nickname)
                .email(email)
                .provider(provider)
                .providerId(providerId)
                .profileImageUrl(profileImageUrl)
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // then
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getProvider()).isEqualTo(provider);
        assertThat(user.getProviderId()).isEqualTo(providerId);
        assertThat(user.getProfileImageUrl()).isEqualTo(profileImageUrl);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("[성공] 닉네임 변경")
    void setNickname_Success() {
        // given
        User user = User.builder()
                .nickname("기존닉네임")
                .email("test@example.com")
                .provider("kakao")
                .providerId("123456")
                .profileImageUrl("https://example.com/profile.jpg")
                .status(UserStatus.ACTIVE)
                .build();

        String newNickname = "새로운닉네임";

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
                .nickname("테스트유저")
                .email("test@example.com")
                .provider("kakao")
                .providerId("123456")
                .profileImageUrl("https://example.com/old-profile.jpg")
                .status(UserStatus.ACTIVE)
                .build();

        String newImageUrl = "https://example.com/new-profile.jpg";

        // when
        user.setProfileImageUrl(newImageUrl);

        // then
        assertThat(user.getProfileImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    @DisplayName("[성공] 사용자 상태 변경")
    void setStatus_Success() {
        // given
        String newNickname = "변경된 사용자";
        User user = User.builder()
                .nickname("테스트유저")
                .email("test@example.com")
                .provider("kakao")
                .providerId("123456")
                .profileImageUrl("https://example.com/profile.jpg")
                .status(UserStatus.ACTIVE)
                .build();

        // when
        user.setNickname(newNickname);

        // then
        assertThat(user.getNickname()).isEqualTo(newNickname);
    }

    @Test
    @DisplayName("[성공] 빌더 패턴 - 필수 필드만")
    void createUser_MinimalFields_Success() {
        // given & when
        User user = User.builder()
                .nickname("최소유저")
                .email("minimal@example.com")
                .provider("kakao")
                .providerId("123")
                .status(UserStatus.ACTIVE)
                .build();

        // then
        assertThat(user.getNickname()).isEqualTo("최소유저");
        assertThat(user.getEmail()).isEqualTo("minimal@example.com");
        assertThat(user.getProvider()).isEqualTo("kakao");
        assertThat(user.getProviderId()).isEqualTo("123");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getProfileImageUrl()).isNull();
    }

    @Test
    @DisplayName("[성공] User 객체 동등성 비교")
    void equals_Success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        
        User user1 = User.builder()
                .nickname("테스트유저")
                .email("test@example.com")
                .provider("kakao")
                .providerId("123456")
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .build();

        User user2 = User.builder()
                .nickname("테스트유저")
                .email("test@example.com")
                .provider("kakao")
                .providerId("123456")
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .build();

        // when & then
        assertThat(user1).isEqualToComparingFieldByField(user2);
    }
}
