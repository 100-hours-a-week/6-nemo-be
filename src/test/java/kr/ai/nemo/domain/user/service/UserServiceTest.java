package kr.ai.nemo.domain.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;
import kr.ai.nemo.domain.user.dto.MyPageResponse;
import kr.ai.nemo.domain.user.dto.NicknameUpdateRequest;
import kr.ai.nemo.domain.user.dto.UpdateUserImageRequest;
import kr.ai.nemo.domain.user.exception.UserException;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.domain.user.validator.UserValidator;
import kr.ai.nemo.infra.ImageService;

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
    // given
    Long userId = 1L;
    LocalDateTime createdAt = LocalDateTime.now();
    User user = User.builder()
        .id(userId)
        .nickname("테스트유저")
        .email("test@example.com")
        .profileImageUrl("https://example.com/profile.jpg")
        .status(UserStatus.ACTIVE)
        .createdAt(createdAt)
        .build();

    given(userRepository.findUserById(userId)).willReturn(Optional.of(user));

    // when
    MyPageResponse response = userService.getMyPage(userId);

    // then
    assertThat(response.nickname()).isEqualTo("테스트유저");
    assertThat(response.email()).isEqualTo("test@example.com");
    assertThat(response.profileImageUrl()).isEqualTo("https://example.com/profile.jpg");
  }

  @Test
  @DisplayName("[실패] 마이페이지 조회 - 존재하지 않는 사용자")
  void getMyPage_UserNotFound_ThrowException() {
    // given
    Long userId = 999L;
    given(userRepository.findUserById(userId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> userService.getMyPage(userId))
        .isInstanceOf(UserException.class);
  }

  @Test
  @DisplayName("[성공] 닉네임 업데이트")
  void updateMyNickname_Success() {
    // given
    Long userId = 1L;
    String newNickname = "새로운닉네임";
    LocalDateTime createdAt = LocalDateTime.now();

    NicknameUpdateRequest request = new NicknameUpdateRequest(newNickname);

    User user = User.builder()
        .id(userId)
        .nickname("기존닉네임")
        .email("test@example.com")
        .profileImageUrl("https://example.com/profile.jpg")
        .status(UserStatus.ACTIVE)
        .createdAt(createdAt)
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    willDoNothing().given(userValidator).isValidByNickname(newNickname);

    // when
    MyPageResponse response = userService.updateMyNickname(userId, request);

    // then
    assertThat(response.nickname()).isEqualTo(newNickname);
    assertThat(user.getNickname()).isEqualTo(newNickname);
    verify(userValidator).isValidByNickname(newNickname);
  }

  @Test
  @DisplayName("[실패] 닉네임 업데이트 - 존재하지 않는 사용자")
  void updateMyNickname_UserNotFound_ThrowException() {
    // given
    Long userId = 999L;
    NicknameUpdateRequest request = new NicknameUpdateRequest("새닉네임");

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> userService.updateMyNickname(userId, request))
        .isInstanceOf(UserException.class);
  }

  @Test
  @DisplayName("[성공] 프로필 이미지 업데이트")
  void updateUserImage_Success() {
    // given
    Long userId = 1L;
    String profileImage = "base64-encoded-image-or-path";
    String oldImageUrl = "https://example.com/old-profile.jpg";
    String newImageUrl = "https://example.com/new-profile.jpg";
    LocalDateTime createdAt = LocalDateTime.now();

    UpdateUserImageRequest request = new UpdateUserImageRequest(profileImage);

    User user = User.builder()
        .id(userId)
        .nickname("테스트유저")
        .email("test@example.com")
        .profileImageUrl(oldImageUrl)
        .status(UserStatus.ACTIVE)
        .createdAt(createdAt)
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(imageService.updateUserImage(oldImageUrl, profileImage, userId)).willReturn(newImageUrl);

    // when
    MyPageResponse response = userService.updateUserImage(userId, request);

    // then
    assertThat(response.profileImageUrl()).isEqualTo(newImageUrl);
    assertThat(user.getProfileImageUrl()).isEqualTo(newImageUrl);
    verify(imageService).updateUserImage(oldImageUrl, profileImage, userId);
  }

  @Test
  @DisplayName("[실패] 프로필 이미지 업데이트 - 존재하지 않는 사용자")
  void updateUserImage_UserNotFound_ThrowException() {
    // given
    Long userId = 999L;
    String profileImage = "base64-encoded-image-or-path";
    UpdateUserImageRequest request = new UpdateUserImageRequest(profileImage);

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> userService.updateUserImage(userId, request))
        .isInstanceOf(UserException.class);
  }

  @Test
  @DisplayName("[성공] 닉네임 업데이트 - 기존과 동일한 닉네임")
  void updateMyNickname_SameNickname_Success() {
    // given
    Long userId = 1L;
    String nickname = "기존닉네임";
    LocalDateTime createdAt = LocalDateTime.now();

    NicknameUpdateRequest request = new NicknameUpdateRequest(nickname);

    User user = User.builder()
        .id(userId)
        .nickname(nickname)
        .email("test@example.com")
        .profileImageUrl("https://example.com/profile.jpg")
        .status(UserStatus.ACTIVE)
        .createdAt(createdAt)
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    willDoNothing().given(userValidator).isValidByNickname(nickname);

    // when
    MyPageResponse response = userService.updateMyNickname(userId, request);

    // then
    assertThat(response.nickname()).isEqualTo(nickname);
    verify(userValidator).isValidByNickname(nickname);
  }

  @Test
  @DisplayName("[성공] 빈 문자열이 아닌 유효한 닉네임으로 업데이트")
  void updateMyNickname_ValidNickname_Success() {
    // given
    Long userId = 1L;
    String validNickname = "유효한닉네임123";
    LocalDateTime createdAt = LocalDateTime.now();

    NicknameUpdateRequest request = new NicknameUpdateRequest(validNickname);

    User user = User.builder()
        .nickname("기존닉네임")
        .email("test@example.com")
        .provider("kakao")
        .providerId("123456")
        .profileImageUrl("https://example.com/profile.jpg")
        .status(UserStatus.ACTIVE)
        .createdAt(createdAt)
        .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    willDoNothing().given(userValidator).isValidByNickname(validNickname);

    // when
    MyPageResponse response = userService.updateMyNickname(userId, request);

    // then
    assertThat(response.nickname()).isEqualTo(validNickname);
    assertThat(response.email()).isEqualTo("test@example.com");
    assertThat(response.profileImageUrl()).isEqualTo("https://example.com/profile.jpg");

    verify(userRepository).findById(userId);
    verify(userValidator).isValidByNickname(validNickname);
  }
}
