package kr.ai.nemo.performance;

import kr.ai.nemo.performance.PerformanceTestExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * 🔬 성능 차이 증명을 위한 데모 테스트
 * 
 * 실제로 다른 방식들의 성능 차이를 보여줍니다.
 */
@ExtendWith(PerformanceTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("🔬 성능 차이 증명 데모")
class PerformanceComparisonDemo {

    @Test
    @DisplayName("⚡ 초고속 테스트 (Standalone 시뮬레이션)")
    void superFastTest() {
        // Standalone 방식: Spring Context 없이 순수 Mock만 사용
        // 실행 시간: ~1-5ms
        
        long start = System.nanoTime();
        
        // Mock 객체 생성 시뮬레이션
        Object mockService = new Object();
        Object mockController = new Object();
        
        // 간단한 로직 실행
        String result = "success";
        
        long end = System.nanoTime();
        long durationMs = (end - start) / 1_000_000;
        
        System.out.println("   💡 실제 소요 시간: " + durationMs + "ms (나노초 정밀도)");
    }

    @Test
    @DisplayName("🏃 빠른 테스트 (WebMvcTest 시뮬레이션)")
    void fastTest() throws InterruptedException {
        // @WebMvcTest 방식: Spring MVC Context 로딩
        // 실행 시간: ~100-500ms
        
        // Spring Context 로딩 시뮬레이션
        Thread.sleep(150); // 150ms 지연
        
        // MVC 테스트 로직 시뮬레이션
        for (int i = 0; i < 10; i++) {
            String mockResponse = "response" + i;
        }
        
        System.out.println("   💡 Spring MVC Context 로딩 시뮬레이션 완료");
    }

    @Test
    @DisplayName("🐌 느린 테스트 (SpringBootTest 시뮬레이션)")
    void slowTest() throws InterruptedException {
        // @SpringBootTest 방식: 전체 Application Context 로딩
        // 실행 시간: ~1-5초
        
        System.out.println("   🔄 전체 Spring Context 로딩 중...");
        
        // 전체 Context 로딩 시뮬레이션
        Thread.sleep(800); // 800ms 지연
        
        // DB, Security, WebSocket 등 모든 컴포넌트 로딩 시뮬레이션
        for (int i = 0; i < 100; i++) {
            String component = "component" + i;
        }
        
        System.out.println("   💡 전체 시스템 로딩 시뮬레이션 완료");
    }

    @Test
    @DisplayName("🚀 실제 측정 방법 안내")
    void measurementGuide() {
        System.out.println("📏 실제 성능 측정 방법:");
        System.out.println("");
        System.out.println("1️⃣ IntelliJ에서 개별 실행:");
        System.out.println("   → GroupControllerStandaloneTest 우클릭 → Run");
        System.out.println("   → Test Results 탭에서 실행 시간 확인");
        System.out.println("   → GroupControllerTest도 같은 방식으로 실행");
        System.out.println("");
        System.out.println("2️⃣ Gradle 명령어:");
        System.out.println("   → ./gradlew test --tests '*StandaloneTest' --info");
        System.out.println("   → ./gradlew test --tests 'GroupControllerTest' --info");
        System.out.println("   → 출력에서 'completed in XXms' 찾기");
        System.out.println("");
        System.out.println("3️⃣ 터미널에서 시간 측정:");
        System.out.println("   → time ./gradlew cleanTest test --tests '*StandaloneTest'");
        System.out.println("   → time ./gradlew cleanTest test --tests 'GroupControllerTest'");
        System.out.println("");
        System.out.println("🎯 예상 결과:");
        System.out.println("   ⚡ Standalone: ~100-500ms (Context 로딩 없음)");
        System.out.println("   🏃 WebMvcTest: ~1-3초 (MVC Context 로딩)");
        System.out.println("   🐌 SpringBootTest: ~3-10초 (전체 Context 로딩)");
    }

    @AfterAll
    void printPerformanceSummary() {
        System.out.println("");
        System.out.println("🎯 데모 테스트 완료!");
        System.out.println("실제 프로젝트 테스트들을 실행해서 진짜 성능 차이를 확인해보세요:");
        System.out.println("");
        System.out.println("  ./gradlew test --tests 'GroupControllerStandaloneTest'");
        System.out.println("  ./gradlew test --tests 'GroupControllerTest'");
        System.out.println("");
        
        // 성능 요약 출력
        PerformanceTestExtension.printSummary();
    }
}
