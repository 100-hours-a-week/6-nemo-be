package kr.ai.nemo.user.validator;

import kr.ai.nemo.user.domain.User;
import kr.ai.nemo.user.domain.enums.UserStatus;
import kr.ai.nemo.user.exception.UserErrorCode;
import kr.ai.nemo.user.exception.UserException;
import kr.ai.nemo.user.repository.UserRepository;
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
