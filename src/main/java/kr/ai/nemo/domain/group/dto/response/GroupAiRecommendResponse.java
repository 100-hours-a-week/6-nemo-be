package kr.ai.nemo.domain.group.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import kr.ai.nemo.domain.group.domain.Group;

public record GroupAiRecommendResponse (
    @JsonProperty("groupId")
    Long groupId,
    String reason,
    Group group
) {}
