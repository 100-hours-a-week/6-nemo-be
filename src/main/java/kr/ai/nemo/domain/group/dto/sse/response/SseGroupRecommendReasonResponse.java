package kr.ai.nemo.domain.group.dto.sse.response;

import kr.ai.nemo.domain.group.domain.enums.AiMessageType;

public record SseGroupRecommendReasonResponse(
    AiMessageType type,
    Reason payload
) {
  public record Reason(
      String reason
  ) {}
}
