package kr.ai.nemo.aop.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Profile({"local", "dev"})
public class TimeTraceAspect {

  private static final Logger log = LoggerFactory.getLogger(TimeTraceAspect.class);

  // 1) 포인트컷 선언: @TimeTrace 애노테이션이 붙은 메서드에 적용
  @Pointcut("@annotation(kr.ai.nemo.aop.logging.TimeTrace)")
  private void timeTraceAnnotation() {}

  // 2) 포인트컷 선언: 특정 패키지 내 컨트롤러 클래스의 모든 메서드 대상
  @Pointcut("within(kr.ai.nemo.domain..controller..*) || within(kr.ai.nemo.domain..service..*)")
  private void controllerPackage() {}

  // 3) 위 두 포인트컷을 합쳐서 advice 적용
  @Around("timeTraceAnnotation() || controllerPackage()")
  public Object measureMethodTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();

    try {
      return joinPoint.proceed();
    } finally {
      long end = System.currentTimeMillis();
      long duration = end - start;
      String methodName = joinPoint.getSignature().toShortString();

      if (duration > 1000) {
        log.warn("⏱️ {} 실행 시간: {}ms (WARN: 1초 초과)", methodName, duration);
      } else {
        log.info("⏱️ {} 실행 시간: {}ms", methodName, duration);
      }
    }
  }
}

