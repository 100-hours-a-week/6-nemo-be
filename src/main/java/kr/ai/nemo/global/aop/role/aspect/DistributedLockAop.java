package kr.ai.nemo.global.aop.role.aspect;

import java.lang.reflect.Method;
import kr.ai.nemo.global.aop.role.annotation.DistributedLock;
import kr.ai.nemo.global.util.AopForTransaction;
import kr.ai.nemo.global.util.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)  // 가장 먼저 실행
public class DistributedLockAop {

  private static final String REDISSON_LOCK_PREFIX = "LOCK:";

  private final RedissonClient redissonClient;
  private final AopForTransaction aopForTransaction;

  @Around("@annotation(kr.ai.nemo.global.aop.role.annotation.DistributedLock)")
  public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();
    DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

    String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(
        signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());

    RLock rLock = redissonClient.getLock(key);

    try {
      boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(),
          distributedLock.timeUnit());
      if (!available) {
        log.warn("Lock acquisition failed for key: {}", key);
        return false;
      }

      // 락 획득 성공 시 실제 비즈니스 로직 실행
      return aopForTransaction.proceed(joinPoint);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw e;
    } finally {
      if (rLock.isHeldByCurrentThread()) {
        rLock.unlock();
        log.info("Lock released for key: {}", key);
      }
    }
  }
}

