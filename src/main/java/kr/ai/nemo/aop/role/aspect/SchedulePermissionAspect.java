package kr.ai.nemo.aop.role.aspect;

import kr.ai.nemo.domain.schedule.exception.ScheduleErrorCode;
import kr.ai.nemo.domain.schedule.exception.ScheduleException;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
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

  @Before("@annotation(kr.ai.nemo.aop.role.annotation.RequireScheduleOwner)")
  public void checkScheduleOwner(JoinPoint joinPoint) {

    Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Long scheduleId = (Long) joinPoint.getArgs()[0];

    if (!scheduleRepository.existsByIdAndOwnerId(scheduleId, userId)) {
      throw new ScheduleException(ScheduleErrorCode.SCHEDULE_DELETE_FORBIDDEN);
    }
  }
}
