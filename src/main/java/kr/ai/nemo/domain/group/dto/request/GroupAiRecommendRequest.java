package kr.ai.nemo.domain.group.dto.request;

public record GroupAiRecommendRequest (
    Long userId,
    String requestText
) {}
