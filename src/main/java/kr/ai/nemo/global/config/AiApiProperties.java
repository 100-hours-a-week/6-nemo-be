package kr.ai.nemo.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.service")
public record AiApiProperties(
    String url,
    Endpoints endpoints
) {

  public record Endpoints(
      String groupGenerate,
      GroupChatbot groupChatbot
  ) {
  }

  public record GroupChatbot(
      String groupRecommendFreeform,
      String groupRecommendQuestions,
      String groupRecommend
  ) {
  }

  public String getGroupGenerateUrl() {
    return url() + endpoints().groupGenerate();  // 메서드로 접근
  }

  public String getGroupRecommendFreeformUrl() {
    return url() + endpoints().groupChatbot().groupRecommendFreeform();
  }

  public String getGroupRecommendQuestionsUrl() {
    return url() + endpoints().groupChatbot().groupRecommendQuestions();
  }

  public String getGroupRecommendUrl() {
    return url() + endpoints().groupChatbot().groupRecommend();
  }
}
