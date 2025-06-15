package kr.ai.nemo.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(AiApiProperties.class)
public class AppConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public RestClient.Builder restClient() {
    return RestClient.builder();
  }
}
