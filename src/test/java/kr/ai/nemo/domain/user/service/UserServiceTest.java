package kr.ai.nemo.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.dto.MyPageResponse;
import kr.ai.nemo.domain.user.dto.NicknameUpdateRequest;
import kr.ai.nemo.domain.user.dto.NicknameUpdateResponse;
import kr.ai.nemo.domain.user.dto.UpdateUserImageRequest;
import kr.ai.nemo.domain.user.exception.UserException;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.domain.user.validator.UserValidator;
import kr.ai.nemo.global.fixture.user.UserFixture;
import kr.ai.nemo.infra.ImageService;
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

  @Mock
  private ImageService imageService;

  @InjectMocks
  private UserService userService;

  @Test
  @DisplayName("[성공] 마이페이지 조회")
  void getMyPage_Success() {
    Long userId = 1L;

    // given
    // userRepository.findDtoById 호출 시 원하는 결과 반환하도록 mocking
    User user = UserFixture.createDefaultUser();
    given(userRepository.findUserById(userId).orElseThrow()).willReturn(user);

    // when
    MyPageResponse response = userService.getMyPage(userId);

    // then
    verify(userRepository).findUserById(userId);
    assertThat(response.nickname()).isEqualTo(user.getNickname());
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
    MyPageResponse response = userService.updateMyNickname(userId, request);

    // then
    assertThat(response.nickname()).isEqualTo(newNickname);
    assertThat(user.getNickname()).isEqualTo(newNickname);
    verify(userValidator).isValidByNickname(newNickname);
  }

  @Test
  @DisplayName("[실패] 닉네임 변경 - 존재하지 않는 사용자")
  void updateNickname_UserNotFound_ThrowException() {
    // given
    Long userId = 999L;
    NicknameUpdateRequest request = new NicknameUpdateRequest("newNickname");

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> userService.updateMyNickname(userId, request))
            .isInstanceOf(UserException.class);
  }

  @Test
  @DisplayName("[성공] 사용자 이미지 업데이트")
  void updateUserImage_Success() {
    // given
    Long userId = 1L;
    User user = UserFixture.createDefaultUser();
    user.setProfileImageUrl("old-image-url");
    
    String newImageUrl = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAAAAAAAD//2Q==";
    String uploadedImageUrl = "https://s3.example.com/new-image.jpg";
    UpdateUserImageRequest request = new UpdateUserImageRequest(newImageUrl);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(imageService.updateUserImage("old-image-url", newImageUrl, userId))
            .willReturn(uploadedImageUrl);

    // when
    userService.updateUserImage(userId, request);

    // then
    verify(userRepository).findById(userId);
    verify(imageService).updateUserImage("old-image-url", newImageUrl, userId);
    assertThat(user.getProfileImageUrl()).isEqualTo(uploadedImageUrl);
  }

  @Test
  @DisplayName("[실패] 사용자 이미지 업데이트 - 존재하지 않는 사용자")
  void updateUserImage_UserNotFound_ThrowException() {
    // given
    Long userId = 999L;
    UpdateUserImageRequest request = new UpdateUserImageRequest("new-image-url");

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> userService.updateUserImage(userId, request))
            .isInstanceOf(UserException.class);
  }
}
