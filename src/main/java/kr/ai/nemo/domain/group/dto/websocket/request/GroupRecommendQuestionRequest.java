package kr.ai.nemo.domain.group.dto.websocket.request;

public record GroupRecommendQuestionRequest(
    String type,
    Payload payload
) {
  public record Payload(
      String sessionId,
      Long userId,
      String answer
  ) {}
}
