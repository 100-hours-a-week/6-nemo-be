package kr.ai.nemo.unit.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.service")
public record AiApiProperties(
    String url,
    Endpoints endpoints
) {

  public record Endpoints(
      String groupGenerate,
      GroupChatbot groupChatbot,
      GroupData groupData
  ) {
  }

  public record GroupChatbot(
      String groupRecommendFreeform,
      String groupRecommendQuestions,
      String groupRecommend
  ) {
  }

  public record GroupData(
      String groupCreate,
      String groupDelete,
      String groupJoin,
      String groupLeave
  ) {
  }

  // 모임 정보 생성 호출 API
  public String getGroupGenerateUrl() {
    return url() + endpoints().groupGenerate();  // 메서드로 접근
  }

  // 텍스트 기반 모임 추천 요청 API
  public String getGroupRecommendFreeformUrl() {
    return url() + endpoints().groupChatbot().groupRecommendFreeform();
  }

  // 선택지 기반 질문 요청 API
  public String getGroupRecommendQuestionsUrl() {
    return url() + endpoints().groupChatbot().groupRecommendQuestions();
  }

  // 선택지 기반 모임 추천 요청 API
  public String getGroupRecommendUrl() {
    return url() + endpoints().groupChatbot().groupRecommend();
  }

  public String getGroupCreateUrl() {
    return url() + endpoints().groupData().groupCreate();
  }

  public String getGroupDeleteUrl() {
    return url() + endpoints().groupData().groupDelete();
  }

  public String getGroupJoinUrl() {
    return url() + endpoints().groupData().groupJoin();
  }

  public String getGroupLeaveUrl() {
    return url() + endpoints().groupData().groupLeave();
  }
}
