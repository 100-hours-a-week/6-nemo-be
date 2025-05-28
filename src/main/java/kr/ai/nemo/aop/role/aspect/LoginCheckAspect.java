package kr.ai.nemo.aop.role.aspect;

import kr.ai.nemo.domain.auth.exception.AuthErrorCode;
import kr.ai.nemo.domain.auth.exception.AuthException;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;
import kr.ai.nemo.domain.user.exception.UserErrorCode;
import kr.ai.nemo.domain.user.exception.UserException;
import kr.ai.nemo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class LoginCheckAspect {

  private final UserRepository userRepository;
  private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

  @Before("@annotation(kr.ai.nemo.aop.role.annotation.CheckLogin)")
  public void checkLogin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      throw new AuthException(AuthErrorCode.UNAUTHORIZED);
    }

    Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    User user = userRepository.findById(userId).orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

    if (UserStatus.WITHDRAWN.equals(user.getStatus())){
      throw new UserException(UserErrorCode.USER_WITHDRAWN);
    }

    CURRENT_USER.set(user);
  }

  @After("@annotation(kr.ai.nemo.aop.role.annotation.CheckLogin)")
  public void cleanup() {
    CURRENT_USER.remove();
  }

  public static User getCurrentUser() {
    return CURRENT_USER.get();
  }
}
