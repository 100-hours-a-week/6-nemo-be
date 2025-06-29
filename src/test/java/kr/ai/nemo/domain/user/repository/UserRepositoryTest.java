package kr.ai.nemo.domain.user.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;

@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("User 도메인 전체 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    // ========== 도메인 테스트 ==========
    
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
                .profileImageUrl("https://example.com/default.jpg")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
                .profileImageUrl("https://example.com/default.jpg")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // then
        assertThat(user.getOwnedGroups()).isNotNull().isEmpty();
        assertThat(user.getGroupParticipants()).isNotNull().isEmpty();
        assertThat(user.getSchedule()).isNotNull().isEmpty();
        assertThat(user.getScheduleParticipants()).isNotNull().isEmpty();
    }

    // ========== 리포지토리 테스트 ==========

    @Test
    @DisplayName("[성공] 사용자 ID로 조회")
    void findUserById_Success() {
        // given
        User user = createUser("test@example.com", "testuser", "kakao", "123456");
        User savedUser = userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findUserById(savedUser.getId());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getNickname()).isEqualTo("testuser");
        assertThat(foundUser.get().getProvider()).isEqualTo("kakao");
        assertThat(foundUser.get().getProviderId()).isEqualTo("123456");
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 사용자 ID로 조회")
    void findUserById_NotFound() {
        // given
        Long nonExistentUserId = 999L;

        // when
        Optional<User> foundUser = userRepository.findUserById(nonExistentUserId);

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("[성공] Provider와 ProviderId로 사용자 조회")
    void findByProviderAndProviderId_Success() {
        // given
        String provider = "google";
        String providerId = "google123";
        User user = createUser("test@gmail.com", "googleuser", provider, providerId);
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByProviderAndProviderId(provider, providerId);

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getProvider()).isEqualTo(provider);
        assertThat(foundUser.get().getProviderId()).isEqualTo(providerId);
        assertThat(foundUser.get().getEmail()).isEqualTo("test@gmail.com");
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 Provider와 ProviderId로 조회")
    void findByProviderAndProviderId_NotFound() {
        // given
        String provider = "facebook";
        String providerId = "facebook123";

        // when
        Optional<User> foundUser = userRepository.findByProviderAndProviderId(provider, providerId);

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("[성공] 사용자 생성 및 저장")
    void saveUser_Success() {
        // given
        User user = createUser("new@example.com", "newuser", "kakao", "kakao789");

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("newuser");
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("[성공] 닉네임으로 사용자 존재 여부 확인")
    void existsByNickname_Success() {
        // given
        String nickname = "existinguser";
        User user = createUser("exist@example.com", nickname, "google", "google999");
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByNickname(nickname);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 닉네임 확인")
    void existsByNickname_NotFound() {
        // given
        String nickname = "nonexistentuser";

        // when
        boolean exists = userRepository.existsByNickname(nickname);

        // then
        assertThat(exists).isFalse();
    }

    private User createUser(String email, String nickname, String provider, String providerId) {
        LocalDateTime now = LocalDateTime.now();
        return User.builder()
                .email(email)
                .nickname(nickname)
                .provider(provider)
                .providerId(providerId)
                .profileImageUrl("https://example.com/default-profile.jpg")
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
