package kr.ai.nemo.domain.user.validator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import kr.ai.nemo.domain.user.exception.UserException;
import kr.ai.nemo.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserValidator 테스트")
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidator userValidator;

    @Test
    @DisplayName("[성공] 사용 가능한 닉네임 검증")
    void isValidByNickname_AvailableNickname_Success() {
        // given
        String nickname = "newNickname";
        given(userRepository.existsByNickname(nickname)).willReturn(false);

        // when
        userValidator.isValidByNickname(nickname);

        // then
        verify(userRepository).existsByNickname(nickname);
    }

    @Test
    @DisplayName("[실패] 이미 사용 중인 닉네임 검증")
    void isValidByNickname_AlreadyUsedNickname_ThrowException() {
        // given
        String nickname = "existingNickname";
        given(userRepository.existsByNickname(nickname)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userValidator.isValidByNickname(nickname))
                .isInstanceOf(UserException.class);
        
        verify(userRepository).existsByNickname(nickname);
    }
}
