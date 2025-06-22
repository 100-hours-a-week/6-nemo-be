package kr.ai.nemo.domain.user.service;

import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.dto.MyPageResponse;
import kr.ai.nemo.domain.user.dto.NicknameUpdateRequest;
import kr.ai.nemo.domain.user.dto.UpdateUserImageRequest;
import kr.ai.nemo.domain.user.exception.UserErrorCode;
import kr.ai.nemo.domain.user.exception.UserException;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.domain.user.validator.UserValidator;
import kr.ai.nemo.infra.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserValidator userValidator;
  private final ImageService imageService;

  @Cacheable(value = "user-profile", key = "#userId")
  @Transactional(readOnly = true)
  public MyPageResponse getMyPage(Long userId) {
    User user = userRepository.findUserById(userId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    
    return new MyPageResponse(
        user.getNickname(),
        user.getEmail(), 
        user.getProfileImageUrl(),
        user.getCreatedAt()
    );
  }

  @CachePut(value = "user-profile", key = "#userId")
  @Transactional
  public MyPageResponse updateMyNickname(Long userId, NicknameUpdateRequest request) {
    String newNickname = request.nickname();
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    userValidator.isValidByNickname(newNickname);
    user.setNickname(newNickname);

    return new MyPageResponse(
        user.getNickname(),
        user.getEmail(),
        user.getProfileImageUrl(),
        user.getCreatedAt()
    );
  }

  @CachePut(value = "user-profile", key = "#userId")
  @Transactional
  public MyPageResponse updateUserImage(Long userId, UpdateUserImageRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

    user.setProfileImageUrl(imageService.updateUserImage(user.getProfileImageUrl(), request.profileImage(), userId));

    return new MyPageResponse(
        user.getNickname(),
        user.getEmail(),
        user.getProfileImageUrl(),
        user.getCreatedAt()
    );
  }
}
