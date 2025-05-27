package kr.ai.nemo.aop.role.aspect;

import kr.ai.nemo.domain.schedule.exception.ScheduleErrorCode;
import kr.ai.nemo.domain.schedule.exception.ScheduleException;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.domain.enums.UserStatus;
import kr.ai.nemo.domain.user.exception.UserErrorCode;
import kr.ai.nemo.domain.user.exception.UserException;
import kr.ai.nemo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class SchedulePermissionAspect {

  private final ScheduleRepository scheduleRepository;
  private final UserRepository userRepository;

  @Before("@annotation(kr.ai.nemo.aop.role.annotation.RequireScheduleOwner)")
  public void checkScheduleOwner(JoinPoint joinPoint) {

    Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Long scheduleId = (Long) joinPoint.getArgs()[0];

    User user = userRepository.findById(userId).orElseThrow(() -> new UserException(
        UserErrorCode.USER_NOT_FOUND));

    if (user.getStatus().equals(UserStatus.WITHDRAWN)) {
      throw new UserException(UserErrorCode.USER_WITHDRAWN);
    }

    if (!scheduleRepository.existsByIdAndOwnerId(scheduleId, userId)) {
      throw new ScheduleException(ScheduleErrorCode.SCHEDULE_DELETE_FORBIDDEN);
    }
  }
}
