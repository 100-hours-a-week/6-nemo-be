package kr.ai.nemo.config;

import java.util.concurrent.Executor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class VirtualThreadTestConfig {

    @Bean
    @Primary
    public Executor taskExecutor() {
        return task -> Thread.ofVirtual().start(task);
    }
}
