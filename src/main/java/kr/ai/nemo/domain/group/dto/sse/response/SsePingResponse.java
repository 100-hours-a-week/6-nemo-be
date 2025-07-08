package kr.ai.nemo.domain.group.dto.sse.response;

import kr.ai.nemo.domain.group.domain.enums.AiMessageType;

public record SsePingResponse (
    AiMessageType type,
    String text
) {

}
