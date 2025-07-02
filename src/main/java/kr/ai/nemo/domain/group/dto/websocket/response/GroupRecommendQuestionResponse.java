package kr.ai.nemo.domain.group.dto.websocket.response;

public record GroupRecommendQuestionResponse(
    String type,
    Payload payload
) {
  public record Payload(
      String sessionId,
      String text
  ) {}
}
