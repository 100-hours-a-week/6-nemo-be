package kr.ai.nemo.performance;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🔬 테스트 성능 측정을 위한 JUnit Extension
 * 
 * 각 테스트의 실행 시간을 자동으로 측정하고 출력합니다.
 */
public class PerformanceTestExtension implements BeforeEachCallback, AfterEachCallback {
    
    private static final ConcurrentHashMap<String, Long> startTimes = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> executionTimes = new ConcurrentHashMap<>();
    
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        String testKey = getTestKey(context);
        startTimes.put(testKey, System.currentTimeMillis());
    }
    
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        String testKey = getTestKey(context);
        Long startTime = startTimes.get(testKey);
        
        if (startTime != null) {
            long executionTime = System.currentTimeMillis() - startTime;
            executionTimes.put(testKey, executionTime);
            
            // 성능 정보 출력
            String testName = context.getDisplayName();
            String className = context.getTestClass().map(Class::getSimpleName).orElse("Unknown");
            
            System.out.println("⏱️  [" + className + "] " + testName + ": " + executionTime + "ms");
            
            // 느린 테스트 경고
            if (executionTime > 1000) {
                System.out.println("   🐌 경고: 1초 이상 소요된 느린 테스트");
            } else if (executionTime < 10) {
                System.out.println("   ⚡ 빠른 테스트!");
            }
        }
    }
    
    private String getTestKey(ExtensionContext context) {
        return context.getTestClass().map(Class::getName).orElse("") + 
               "#" + context.getTestMethod().map(m -> m.getName()).orElse("");
    }
    
    /**
     * 성능 측정 결과 요약 출력
     */
    public static void printSummary() {
        if (executionTimes.isEmpty()) {
            System.out.println("📊 측정된 테스트가 없습니다.");
            return;
        }
        
        System.out.println("");
        System.out.println("📊 테스트 성능 요약:");
        System.out.println("================================");
        
        executionTimes.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e1.getValue(), e2.getValue()))
            .forEach(entry -> {
                String testName = entry.getKey().substring(entry.getKey().lastIndexOf("#") + 1);
                String className = entry.getKey().substring(0, entry.getKey().lastIndexOf("#"));
                className = className.substring(className.lastIndexOf(".") + 1);
                
                String icon = entry.getValue() < 10 ? "⚡" : 
                             entry.getValue() < 100 ? "🚀" : 
                             entry.getValue() < 1000 ? "🏃" : "🐌";
                
                System.out.println(icon + " " + className + "." + testName + ": " + entry.getValue() + "ms");
            });
        
        // 통계
        long fastest = executionTimes.values().stream().min(Long::compare).orElse(0L);
        long slowest = executionTimes.values().stream().max(Long::compare).orElse(0L);
        double average = executionTimes.values().stream().mapToLong(Long::longValue).average().orElse(0.0);
        
        System.out.println("--------------------------------");
        System.out.println("⚡ 가장 빠른 테스트: " + fastest + "ms");
        System.out.println("🐌 가장 느린 테스트: " + slowest + "ms");
        System.out.println("📊 평균 실행 시간: " + String.format("%.1f", average) + "ms");
        if (slowest > 0) {
            System.out.println("📈 속도 차이: " + String.format("%.1f", slowest / (double) fastest) + "배");
        }
        System.out.println("================================");
    }
}
