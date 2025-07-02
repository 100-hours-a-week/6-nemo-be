package kr.ai.nemo.domain.group.dto.websocket.response;

public record GroupRecommendGroupIdResponse(
    String type,
    GroupIdPayload payload
) {
  public record GroupIdPayload(
      String sessionId,
      Long groupId
  ) {}
}
