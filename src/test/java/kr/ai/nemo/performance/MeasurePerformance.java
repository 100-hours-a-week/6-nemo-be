package kr.ai.nemo.performance;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 성능 측정을 활성화하는 어노테이션
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@interface MeasurePerformance {
  /**
   * 경고를 표시할 최대 실행 시간 (ms)
   */
  long maxDurationMs() default 1000;
}
