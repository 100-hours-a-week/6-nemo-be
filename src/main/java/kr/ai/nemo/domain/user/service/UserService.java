package kr.ai.nemo.domain.user.service;

import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.dto.MyPageResponse;
import kr.ai.nemo.domain.user.dto.NicknameUpdateRequest;
import kr.ai.nemo.domain.user.dto.NicknameUpdateResponse;
import kr.ai.nemo.domain.user.exception.UserErrorCode;
import kr.ai.nemo.domain.user.exception.UserException;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.domain.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserValidator userValidator;

  @Transactional(readOnly = true)
  public MyPageResponse getMyPage(Long userId) {
    return userRepository.findDtoById(userId);
  }

  @Transactional
  public NicknameUpdateResponse updateMyNickname(Long userId, NicknameUpdateRequest request) {
    String newNickname = request.nickname();
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    userValidator.isValidByNickname(newNickname);
    user.setNickname(newNickname);
    return new NicknameUpdateResponse(newNickname);
  }
}
