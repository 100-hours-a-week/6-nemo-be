package kr.ai.nemo.unit.global.aop.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimeTraceAspect 테스트")
class TimeTraceAspectTest {

    @InjectMocks
    private TimeTraceAspect timeTraceAspect;

    @Test
    @DisplayName("[성공] 메서드 실행 시간 측정 - 정상 수행")
    void measureMethodTime_Success() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        String expectedResult = "test result";

        given(joinPoint.getSignature()).willReturn(signature);
        given(signature.toShortString()).willReturn("TestService.testMethod(..)");
        given(joinPoint.proceed()).willReturn(expectedResult);

        // when
        Object result = timeTraceAspect.measureMethodTime(joinPoint);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(joinPoint).proceed();
        verify(signature).toShortString();
    }

    @Test
    @DisplayName("[성공] 메서드 실행 시간 측정 - 느린 메서드 (1초 초과)")
    void measureMethodTime_SlowMethod_LogWarning() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        String expectedResult = "slow result";

        given(joinPoint.getSignature()).willReturn(signature);
        given(signature.toShortString()).willReturn("SlowService.slowMethod(..)");
        
        // 1초 이상 걸리는 메서드 시뮬레이션
        given(joinPoint.proceed()).willAnswer(invocation -> {
            Thread.sleep(1100); // 1.1초 대기
            return expectedResult;
        });

        // when
        Object result = timeTraceAspect.measureMethodTime(joinPoint);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(joinPoint).proceed();
        verify(signature).toShortString();
    }

    @Test
    @DisplayName("[실패] 메서드 실행 중 예외 발생")
    void measureMethodTime_ExceptionThrown_StillMeasureTime() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        RuntimeException expectedException = new RuntimeException("Test exception");

        given(joinPoint.getSignature()).willReturn(signature);
        given(signature.toShortString()).willReturn("ErrorService.errorMethod(..)");
        doThrow(expectedException).when(joinPoint).proceed();

        // when & then
        assertThatThrownBy(() -> timeTraceAspect.measureMethodTime(joinPoint))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Test exception");

        // 예외가 발생해도 시간 측정은 수행되어야 함
        verify(joinPoint).proceed();
        verify(signature).toShortString();
    }

    @Test
    @DisplayName("[성공] 메서드 실행 시간 측정 - null 반환")
    void measureMethodTime_NullReturn_Success() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        given(joinPoint.getSignature()).willReturn(signature);
        given(signature.toShortString()).willReturn("VoidService.voidMethod(..)");
        given(joinPoint.proceed()).willReturn(null);

        // when
        Object result = timeTraceAspect.measureMethodTime(joinPoint);

        // then
        assertThat(result).isNull();
        verify(joinPoint).proceed();
        verify(signature).toShortString();
    }

    @Test
    @DisplayName("[성공] 메서드 실행 시간 측정 - 빠른 메서드 (1초 미만)")
    void measureMethodTime_FastMethod_LogInfo() throws Throwable {
        // given
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        String expectedResult = "fast result";

        given(joinPoint.getSignature()).willReturn(signature);
        given(signature.toShortString()).willReturn("FastService.fastMethod(..)");
        
        // 빠른 메서드 시뮬레이션 (100ms)
        given(joinPoint.proceed()).willAnswer(invocation -> {
            Thread.sleep(100);
            return expectedResult;
        });

        // when
        Object result = timeTraceAspect.measureMethodTime(joinPoint);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(joinPoint).proceed();
        verify(signature).toShortString();
    }
}
