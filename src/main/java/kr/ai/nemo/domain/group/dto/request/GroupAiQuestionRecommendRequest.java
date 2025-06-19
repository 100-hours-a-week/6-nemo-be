package kr.ai.nemo.domain.group.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GroupAiQuestionRecommendRequest(
    @JsonProperty("userId")
    Long userId,
    List<ContextLog> messages
) {

  public record ContextLog(
      String role,
      String text
  ) {

  }
}
