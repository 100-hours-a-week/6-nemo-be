package kr.ai.nemo.group.dto.response;

import java.util.List;
import kr.ai.nemo.group.dto.request.GroupGenerateRequest;

public record GroupGenerateResponse(
    String name,
    String category,
    String summary,
    String description,
    String plan,
    String location,
    int maxUserCount,
    List<String> tags
) {
  public static GroupGenerateResponse from(GroupGenerateRequest request, GroupAiGenerateResponse ai) {
    return new GroupGenerateResponse(
        request.name(),
        request.category(),
        ai.summary(),
        ai.description(),
        ai.plan(),
        request.location(),
        request.maxUserCount(),
        ai.tags()
    );
  }
}
