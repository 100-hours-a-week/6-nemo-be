package kr.ai.nemo.domain.group.dto.websocket.response;

public record GroupRecommendReasonResponse(
    String type,
    ReasonPayload payload
) {
  public record ReasonPayload(
      String sessionId,
      String reason
  ) {}
}
