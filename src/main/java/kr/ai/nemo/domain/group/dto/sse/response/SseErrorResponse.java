package kr.ai.nemo.domain.group.dto.sse.response;

import kr.ai.nemo.domain.group.domain.enums.AiMessageType;

public record SseErrorResponse(
    AiMessageType type,
    String payload
) {

}
