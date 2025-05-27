package kr.ai.nemo.domain.user.validator;

import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;
import kr.ai.nemo.domain.user.exception.UserErrorCode;
import kr.ai.nemo.domain.user.exception.UserException;
import kr.ai.nemo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserValidator {

  private final UserRepository repository;

  public User findByIdOrThrow(Long userId) {
    User user = repository.findById(userId)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    if (user.getStatus().equals(UserStatus.WITHDRAWN)){
      throw new UserException(UserErrorCode.USER_WITHDRAWN);
    }
    return user;
  }
}
