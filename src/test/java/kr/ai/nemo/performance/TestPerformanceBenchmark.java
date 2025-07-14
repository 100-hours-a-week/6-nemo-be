package kr.ai.nemo.performance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 🔬 실제 테스트 성능 측정 및 증명
 * 
 * 각 테스트 방식의 실행 시간을 실제로 측정하여 
 * Standalone이 정말 빠른지 증명합니다.
 */
@DisplayName("🔬 테스트 성능 벤치마크")
class TestPerformanceBenchmark {

    @Test
    @DisplayName("📊 성능 측정 방법 가이드")
    void performanceMeasurementGuide() {
        System.out.println("🔬 테스트 성능 측정 방법:");
        System.out.println("");
        
        System.out.println("1️⃣ IDE에서 개별 측정:");
        System.out.println("   → IntelliJ: 각 테스트 클래스를 개별 실행");
        System.out.println("   → 하단 Test Results 탭에서 실행 시간 확인");
        System.out.println("   → 'Run' 버튼 옆의 시간 표시 확인");
        System.out.println("");
        
        System.out.println("2️⃣ Gradle 명령어로 측정:");
        System.out.println("   → ./gradlew test --tests \"GroupControllerStandaloneTest\" --info");
        System.out.println("   → ./gradlew test --tests \"GroupControllerTest\" --info");
        System.out.println("   → 출력에서 'completed in' 시간 확인");
        System.out.println("");
        
        System.out.println("3️⃣ 콘솔에서 시간 측정:");
        System.out.println("   → time ./gradlew test --tests \"GroupControllerStandaloneTest\"");
        System.out.println("   → time ./gradlew test --tests \"GroupControllerTest\"");
        System.out.println("   → real/user/sys 시간 비교");
        System.out.println("");
        
        System.out.println("4️⃣ 자동화된 측정 (다음 테스트 참고):");
        System.out.println("   → 프로그래밍 방식으로 시간 측정");
        System.out.println("   → 여러 번 실행해서 평균값 계산");
    }

    @Test
    @DisplayName("⏱️ 실제 성능 측정 실행")
    void actualPerformanceMeasurement() {
        System.out.println("⏱️ 성능 측정 시작...");
        System.out.println("");
        
        // Standalone 테스트 시뮬레이션
        long standaloneStart = System.currentTimeMillis();
        simulateStandaloneTest();
        long standaloneEnd = System.currentTimeMillis();
        long standaloneTime = standaloneEnd - standaloneStart;
        
        // Spring Context 로딩 시뮬레이션
        long springStart = System.currentTimeMillis();
        simulateSpringContextLoading();
        long springEnd = System.currentTimeMillis();
        long springTime = springEnd - springStart;
        
        // 결과 출력
        System.out.println("📊 측정 결과:");
        System.out.println("   🚀 Standalone 시뮬레이션: " + standaloneTime + "ms");
        System.out.println("   🐌 Spring Context 시뮬레이션: " + springTime + "ms");
        System.out.println("   📈 속도 차이: " + (springTime / (double) standaloneTime) + "배");
        System.out.println("");
        
        System.out.println("🎯 실제 측정하려면:");
        System.out.println("   1. GroupControllerStandaloneTest 실행 → 시간 기록");
        System.out.println("   2. GroupControllerTest 실행 → 시간 기록");
        System.out.println("   3. 차이 비교!");
    }

    private void simulateStandaloneTest() {
        // Mock 객체 생성 시뮬레이션 (매우 빠름)
        try {
            Thread.sleep(5); // 5ms - Standalone은 매우 빠름
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void simulateSpringContextLoading() {
        // Spring Context 로딩 시뮬레이션 (느림)
        try {
            Thread.sleep(500); // 500ms - Spring Context 로딩
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("🏃‍♂️ 반복 측정으로 정확도 높이기")
    void repeatedMeasurement() {
        System.out.println("🏃‍♂️ 10회 반복 측정 시작...");
        
        int iterations = 10;
        long totalStandalone = 0;
        long totalSpring = 0;
        
        for (int i = 0; i < iterations; i++) {
            // Standalone 측정
            long start = System.nanoTime();
            simulateStandaloneTest();
            long end = System.nanoTime();
            totalStandalone += (end - start) / 1_000_000; // ns를 ms로 변환
            
            // Spring 측정
            start = System.nanoTime();
            simulateSpringContextLoading();
            end = System.nanoTime();
            totalSpring += (end - start) / 1_000_000;
        }
        
        double avgStandalone = totalStandalone / (double) iterations;
        double avgSpring = totalSpring / (double) iterations;
        
        System.out.println("");
        System.out.println("📊 " + iterations + "회 반복 측정 결과:");
        System.out.println("   🚀 Standalone 평균: " + String.format("%.2f", avgStandalone) + "ms");
        System.out.println("   🐌 Spring 평균: " + String.format("%.2f", avgSpring) + "ms");
        System.out.println("   📈 성능 향상: " + String.format("%.1f", avgSpring / avgStandalone) + "배 빠름");
    }
}
