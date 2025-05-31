package kr.ai.nemo.domain.auth.repository;

import kr.ai.nemo.domain.auth.domain.UserToken;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserTokenRepository 테스트")
class UserTokenRepositoryTest {

    @Autowired
    private UserTokenRepository userTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 토큰 저장 테스트")
    void save_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        User savedUser = userRepository.save(user);
        
        UserToken userToken = UserToken.builder()
                .user(savedUser)
                .provider("kakao")
                .refreshToken("test-refresh-token")
                .deviceInfo("WEB")
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // when
        UserToken savedToken = userTokenRepository.save(userToken);
        
        // then
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getRefreshToken()).isEqualTo("test-refresh-token");
        assertThat(savedToken.getProvider()).isEqualTo("kakao");
    }
}
