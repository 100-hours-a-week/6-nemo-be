package kr.ai.nemo.domain.group.dto.response;

public record GroupAiRecommendResponse (
    Long groupId,
    String responseText
) {}
