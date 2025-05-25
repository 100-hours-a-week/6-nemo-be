package kr.ai.nemo.group.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import kr.ai.nemo.group.domain.Group;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record GroupCreateResponse(
    Long groupId,
    String name,
    String category,
    String summary,
    String description,
    String plan,
    String location,
    int currentUserCount,
    int maxUserCount,
    String imageUrl,
    List<String> tags
) {
  public static GroupCreateResponse from(Group group, List<String> tags) {
    return new GroupCreateResponse(
        group.getId(),
        group.getName(),
        group.getCategory(),
        group.getSummary(),
        group.getDescription(),
        group.getPlan(),
        group.getLocation(),
        group.getCurrentUserCount(),
        group.getMaxUserCount(),
        group.getImageUrl(),
        tags
    );
  }
}
