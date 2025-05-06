package kr.ai.nemo.user.service;

import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import kr.ai.nemo.user.domain.User;
import kr.ai.nemo.user.domain.enums.UserStatus;
import kr.ai.nemo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {

  private final UserRepository userRepository;

  public User findByIdOrThrow(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(ResponseCode.USER_NOT_FOUND));
    if (user.getStatus() == UserStatus.WITHDRAWN) {
      throw new CustomException(ResponseCode.USER_WITHDRAWN);
    }

    return user;
  }
}

