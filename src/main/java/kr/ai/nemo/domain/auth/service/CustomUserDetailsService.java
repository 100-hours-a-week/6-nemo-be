package kr.ai.nemo.domain.auth.service;

import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;
import kr.ai.nemo.domain.user.exception.UserErrorCode;
import kr.ai.nemo.domain.user.exception.UserException;
import kr.ai.nemo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetails loadUserById(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

    if (user.getStatus() == UserStatus.WITHDRAWN) {
      throw new UserException(UserErrorCode.USER_WITHDRAWN);
    }

    return new CustomUserDetails(user);
  }
}

