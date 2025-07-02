package kr.ai.nemo.domain.group.dto.websocket.response;

import java.util.List;

public record GroupRecommendOptionResponse(
    String type,
    Payload payload
) {
  public record Payload(
      String sessionId,
      List<String> options
  ) {}
}
