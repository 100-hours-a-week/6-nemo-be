package kr.ai.nemo.groupparticipants.dto.response;

import java.util.List;
import kr.ai.nemo.group.domain.Group;

public record MyGroupDto(
    Long groupId,
    String name,
    String category,
    String summary,
    String location,
    int currentUserCount,
    int maxUserCount,
    String imageUrl,
    List<String> tags
) {
  public static MyGroupDto from(Group group) {
    return new MyGroupDto(
        group.getId(),
        group.getName(),
        group.getCategory(),
        group.getSummary(),
        group.getLocation(),
        group.getCurrentUserCount(),
        group.getMaxUserCount(),
        group.getImageUrl(),
        group.getGroupTags().stream()
            .map(gt -> gt.getTag().getName())
            .toList()
    );
  }
}
