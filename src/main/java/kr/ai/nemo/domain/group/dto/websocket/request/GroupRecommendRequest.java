package kr.ai.nemo.domain.group.dto.websocket.request;

import java.util.List;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRecommendRequest.ContextLog;

public record GroupRecommendRequest(
    String type,
    RequestPayload payload
) {
  public record RequestPayload (
      String sessionId,
      Long userId,
      List<ContextLog> messages
  ) {
  }
}
