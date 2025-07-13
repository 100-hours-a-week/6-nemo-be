package kr.ai.nemo.unit.global.aop.logging;

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

  @Pointcut("@annotation(kr.ai.nemo.unit.global.aop.logging.TimeTrace)")
  private void timeTraceAnnotation() {}

  @Pointcut("within(kr.ai.nemo.domain..controller..*) || within(kr.ai.nemo.domain..service..*)")
  private void applicationPackage() {}

  // Controller든 Service든 어노테이션 있을 때만
  @Around("timeTraceAnnotation() && applicationPackage()")
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

