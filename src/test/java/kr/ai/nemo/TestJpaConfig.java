package kr.ai.nemo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
public class TestJpaConfig {
    
    // JPA Auditing을 비활성화하는 더미 설정
    // 이 설정이 있으면 메인 애플리케이션의 @EnableJpaAuditing을 오버라이드함
}
