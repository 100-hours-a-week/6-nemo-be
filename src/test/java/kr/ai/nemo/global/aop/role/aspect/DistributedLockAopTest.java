package kr.ai.nemo.global.aop.role.aspect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import kr.ai.nemo.global.aop.role.annotation.DistributedLock;
import kr.ai.nemo.global.error.exception.LockAcquisitionFailedException;
import kr.ai.nemo.global.util.AopForTransaction;
import kr.ai.nemo.global.util.CustomSpringELParser;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
@DisplayName("DistributedLockAop 테스트")
class DistributedLockAopTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private AopForTransaction aopForTransaction;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private RLock rLock;

    @InjectMocks
    private DistributedLockAop distributedLockAop;

    @Test
    @DisplayName("[성공] 락 획득 및 메서드 실행")
    void lock_Success() throws Throwable {
        // given
        DistributedLock distributedLock = createDistributedLock();
        Method method = TestService.class.getMethod("testMethod", String.class);
        String[] parameterNames = {"param1"};
        Object[] args = {"value1"};
        String expectedResult = "success";

        given(joinPoint.getSignature()).willReturn(methodSignature);
        given(methodSignature.getMethod()).willReturn(method);
        given(methodSignature.getParameterNames()).willReturn(parameterNames);
        given(joinPoint.getArgs()).willReturn(args);
        
        when(redissonClient.getLock("LOCK:test-key")).thenReturn(rLock);
        when(rLock.tryLock(2L, 5L, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        given(aopForTransaction.proceed(joinPoint)).willReturn(expectedResult);

        try (MockedStatic<CustomSpringELParser> mockedParser = Mockito.mockStatic(CustomSpringELParser.class)) {
            mockedParser.when(() -> CustomSpringELParser.getDynamicValue(parameterNames, args, "'test-key'"))
                    .thenReturn("test-key");

            // when
            Object result = distributedLockAop.lock(joinPoint);

            // then
            assertThat(result).isEqualTo(expectedResult);
            verify(rLock).tryLock(2L, 5L, TimeUnit.SECONDS);
            verify(aopForTransaction).proceed(joinPoint);
            verify(rLock).unlock();
        }
    }

    @Test
    @DisplayName("[실패] 락 획득 실패")
    void lock_AcquisitionFailed_ThrowException() throws Throwable {
        // given
        DistributedLock distributedLock = createDistributedLock();
        Method method = TestService.class.getMethod("testMethod", String.class);
        String[] parameterNames = {"param1"};
        Object[] args = {"value1"};

        given(joinPoint.getSignature()).willReturn(methodSignature);
        given(methodSignature.getMethod()).willReturn(method);
        given(methodSignature.getParameterNames()).willReturn(parameterNames);
        given(joinPoint.getArgs()).willReturn(args);
        
        when(redissonClient.getLock("LOCK:test-key")).thenReturn(rLock);
        when(rLock.tryLock(2L, 5L, TimeUnit.SECONDS)).thenReturn(false);

        try (MockedStatic<CustomSpringELParser> mockedParser = Mockito.mockStatic(CustomSpringELParser.class)) {
            mockedParser.when(() -> CustomSpringELParser.getDynamicValue(parameterNames, args, "'test-key'"))
                    .thenReturn("test-key");

            // when & then
            assertThatThrownBy(() -> distributedLockAop.lock(joinPoint))
                    .isInstanceOf(LockAcquisitionFailedException.class)
                    .hasMessageContaining("Failed to acquire lock for key: LOCK:test-key");

            verify(rLock).tryLock(2L, 5L, TimeUnit.SECONDS);
            verify(aopForTransaction, Mockito.never()).proceed(joinPoint);
        }
    }

    private DistributedLock createDistributedLock() {
        return new DistributedLock() {
            @Override
            public String key() {
                return "'test-key'";
            }

            @Override
            public long waitTime() {
                return 2L;
            }

            @Override
            public long leaseTime() {
                return 5L;
            }

            @Override
            public TimeUnit timeUnit() {
                return TimeUnit.SECONDS;
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DistributedLock.class;
            }
        };
    }

    // 테스트용 서비스 클래스
    public static class TestService {
        @DistributedLock(key = "'test-key'", waitTime = 2, leaseTime = 5)
        public String testMethod(String param1) {
            return "success";
        }
    }
}
