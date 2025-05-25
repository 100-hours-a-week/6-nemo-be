package kr.ai.nemo.group.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import kr.ai.nemo.group.domain.Group;

@JsonInclude(Include.NON_EMPTY)
public record GroupDetailResponse(
    String name,
    String summary,
    String description,
    String category,
    String location,
    int currentUserCount,
    int maxUserCount,
    String imageUrl,
    List<String> tags,
    String plan,
    String ownerName
) {

  public static GroupDetailResponse from(Group group, List<String> tags) {
    return new GroupDetailResponse(
        group.getName(),
        group.getCategory(),
        group.getSummary(),
        group.getDescription(),
        group.getLocation(),
        group.getCurrentUserCount(),
        group.getMaxUserCount(),
        group.getImageUrl(),
        tags,
        group.getPlan(),
        group.getOwner().getNickname()
    );
  }
}
