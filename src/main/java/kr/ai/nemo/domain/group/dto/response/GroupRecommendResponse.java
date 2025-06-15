package kr.ai.nemo.domain.group.dto.response;

public record GroupRecommendResponse(
    GroupDto group,
    String reason
) {
}
