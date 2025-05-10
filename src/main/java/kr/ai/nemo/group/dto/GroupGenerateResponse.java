package kr.ai.nemo.group.dto;

import java.util.List;

public record GroupGenerateResponse(
    String name,
    String goal,
    String category,
    String location,
    String period,
    int maxUserCount,
    boolean isPlanCreated,
    String summary,
    String description,
    List<String> tags,
    String plan
) {
  public static GroupGenerateResponse from(GroupGenerateRequest request, GroupAiGenerateResponse ai) {
    return new GroupGenerateResponse(
        request.name(),
        request.goal(),
        request.category(),
        request.location(),
        request.period(),
        request.maxUserCount(),
        request.isPlanCreated(),
        ai.summary(),
        ai.description(),
        ai.tags(),
        ai.plan()
    );
  }
}
