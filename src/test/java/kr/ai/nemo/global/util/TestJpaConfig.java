package kr.ai.nemo.global.util;

import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
public class TestJpaConfig {
    
    // JPA Auditing을 비활성화하는 더미 설정
    // 이 설정이 있으면 메인 애플리케이션의 @EnableJpaAuditing을 오버라이드함
}
