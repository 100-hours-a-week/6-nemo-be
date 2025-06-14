package kr.ai.nemo.domain.group.dto.response;

public record GroupAiRecommendTextResponse(
    GroupDto group,
    String reason
) {
}
