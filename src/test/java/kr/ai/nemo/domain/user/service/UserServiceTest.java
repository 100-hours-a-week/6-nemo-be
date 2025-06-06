package kr.ai.nemo.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import kr.ai.nemo.domain.user.dto.MyPageResponse;
import kr.ai.nemo.domain.user.repository.UserRepository;
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

  @InjectMocks
  private UserService userService;

  @Test
  @DisplayName("[성공] user 테스트 조회")
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

}
