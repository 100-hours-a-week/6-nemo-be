package kr.ai.nemo.unit.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("NEMO API")
            .version("1.0.0")
            .description("NEMO 프로젝트 API 문서"));
  }

  @Bean
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("nemo-api")
        .pathsToMatch("/**")
        .packagesToExclude("kr.ai.nemo.global.swagger")
        .addOpenApiCustomizer(openApi -> {
          // 여기서 스키마 제거
          if (openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
            openApi.getComponents().getSchemas().entrySet()
                .removeIf(entry ->
                    entry.getKey().startsWith("Swagger") ||
                    entry.getKey().startsWith("Base") ||
                    entry.getKey().endsWith("Dto") ||
                    entry.getKey().endsWith("Info")
                );
          }
        })
        .build();
  }
}
