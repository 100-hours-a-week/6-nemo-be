package kr.ai.nemo.domain.group.dto.sse.response;

import java.util.List;
import kr.ai.nemo.domain.group.domain.enums.AiMessageType;

public record SseGroupQuestionOptionResponse(
    AiMessageType type,
    Option payload
) {

  public record Option(
      List<String> options
  ) { }
}
