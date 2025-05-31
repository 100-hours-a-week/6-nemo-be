package kr.ai.nemo.domain.user.validator;

import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserValidator 테스트")
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidator userValidator;

    @Test
    @DisplayName("사용자 조회 테스트 - 실제 메서드 확인 후 작성 예정")
    void validateUser_Success() {
        // given
        Long userId = 1L;
        User user = UserFixture.createDefaultUser();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when & then
        // UserValidator의 실제 메서드를 확인한 후 테스트 구현 예정
        // userValidator.validateUserExists(userId);
    }
}
