package kr.ai.nemo.global.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOriginPattern("https://dev.nemo.ai.kr");
    configuration.addAllowedOriginPattern("http://localhost:3000");
    configuration.addAllowedOriginPattern("http://localhost:8000");
    configuration.addAllowedOriginPattern("https://localhost:3000");
    configuration.addAllowedOriginPattern("https://onurivit01.store");
    configuration.addAllowedOriginPattern("https://local.dev.nemo.ai.kr:*");
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
