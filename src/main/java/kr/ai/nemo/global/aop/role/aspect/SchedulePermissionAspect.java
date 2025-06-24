package kr.ai.nemo.global.aop.role.aspect;

import kr.ai.nemo.global.aop.role.annotation.RequireScheduleOwner;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.schedule.exception.ScheduleErrorCode;
import kr.ai.nemo.domain.schedule.exception.ScheduleException;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class SchedulePermissionAspect {

  private final ScheduleRepository scheduleRepository;

  @Before("@annotation(requireScheduleOwner)")
  public void checkScheduleOwner(JoinPoint joinPoint, RequireScheduleOwner requireScheduleOwner) {
    CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Long userId = customUserDetails.getUserId();

    // 애노테이션의 value 값: 기본값은 "scheduleId"
    String paramName = requireScheduleOwner.value();
    Long scheduleId = extractScheduleId(joinPoint, paramName);

    if (scheduleId == null) {
      throw new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND);
    }

    if (!scheduleRepository.existsByIdAndOwnerId(scheduleId, userId)) {
      throw new ScheduleException(ScheduleErrorCode.SCHEDULE_DELETE_FORBIDDEN);
    }
  }

  // 메서드 파라미터 목록에서 애노테이션에 지정한 이름("scheduleId")과 일치하는 파라미터 값을 찾아 반환
  private Long extractScheduleId(JoinPoint joinPoint, String paramName) {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String[] parameterNames = methodSignature.getParameterNames();
    Object[] args = joinPoint.getArgs();

    for (int i = 0; i < parameterNames.length; i++) {
      if (parameterNames[i].equals(paramName)) {
        return (Long) args[i];
      }
    }

    throw new IllegalArgumentException(
        String.format("Parameter '%s' not found in method: %s", paramName, methodSignature.getName()));
  }
}
