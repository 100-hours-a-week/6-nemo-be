package kr.ai.nemo.domain.group.dto.request;

import java.util.List;

public record GroupAiQuestionRecommendRequest(
  List<ContextLog> messages
) {
  public record ContextLog(
      String role,
      String text
  ) {
  }
}
