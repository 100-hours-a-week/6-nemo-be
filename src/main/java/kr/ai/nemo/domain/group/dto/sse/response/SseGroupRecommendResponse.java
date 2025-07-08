package kr.ai.nemo.domain.group.dto.sse.response;

import kr.ai.nemo.domain.group.domain.enums.AiMessageType;
import kr.ai.nemo.domain.group.dto.response.GroupDto;

public record SseGroupRecommendResponse(
    AiMessageType type,
    GroupDto payload
) {
}
