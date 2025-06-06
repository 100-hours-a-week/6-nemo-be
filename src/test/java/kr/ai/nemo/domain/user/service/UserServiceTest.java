package kr.ai.nemo.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.dto.MyPageResponse;
import kr.ai.nemo.domain.user.dto.NicknameUpdateRequest;
import kr.ai.nemo.domain.user.dto.NicknameUpdateResponse;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.domain.user.validator.UserValidator;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserValidator userValidator;

  @InjectMocks
  private UserService userService;

  @Test
  @DisplayName("[성공] 마이페이지 조회")
  void getMyPage_Success() {
    Long userId = 1L;

    // given
    // userRepository.findDtoById 호출 시 원하는 결과 반환하도록 mocking
    MyPageResponse mockResponse = new MyPageResponse("test", "test@example.com", "test.jpg", LocalDateTime.now());
    given(userRepository.findDtoById(userId)).willReturn(mockResponse);

    // when
    MyPageResponse response = userService.getMyPage(userId);

    // then
    verify(userRepository).findDtoById(userId);
    assertThat(response.nickname()).isEqualTo("test");
  }

  @Test
  @DisplayName("[성공] 닉네임 변경")
  void updateNickname_Success() {
    // given
    Long userId = 1L;
    User user = UserFixture.createDefaultUser();
    String newNickname = "newNickname";
    NicknameUpdateRequest request = new NicknameUpdateRequest(newNickname);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    doNothing().when(userValidator).isValidByNickname(newNickname);

    // when
    NicknameUpdateResponse response = userService.updateMyNickname(userId, request);

    // then
    assertThat(response.nickname()).isEqualTo(newNickname);
  }
}
