package kr.ai.nemo.domain.user.validator;

import kr.ai.nemo.domain.user.exception.UserErrorCode;
import kr.ai.nemo.domain.user.exception.UserException;
import kr.ai.nemo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {
  private final UserRepository userRepository;

  public void isValidByNickname(String nickname) {
    if(userRepository.existsByNickname(nickname)) {
      throw new UserException(UserErrorCode.ALREADY_USED_NICKNAME);
    }

  }
}
