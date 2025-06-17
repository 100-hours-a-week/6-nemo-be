package kr.ai.nemo.domain.group.dto.response;

import kr.ai.nemo.domain.group.domain.Group;

public record GroupAiRecommendResponse (
    Long groupId,
    String responseText,
    Group group
) {}
