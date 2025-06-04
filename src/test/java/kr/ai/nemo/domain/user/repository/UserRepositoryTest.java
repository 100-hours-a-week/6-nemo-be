package kr.ai.nemo.domain.user.repository;

import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 저장 테스트")
    void save_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        
        // when
        User savedUser = userRepository.save(user);
        
        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getNickname()).isEqualTo("testUser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Provider와 ProviderId로 사용자 조회 테스트")
    void findByProviderAndProviderId_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        userRepository.save(user);
        
        // when
        User foundUser = userRepository.findByProviderAndProviderId("kakao", "123456789").orElse(null);
        
        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getProvider()).isEqualTo("kakao");
        assertThat(foundUser.getProviderId()).isEqualTo("123456789");
    }
}
