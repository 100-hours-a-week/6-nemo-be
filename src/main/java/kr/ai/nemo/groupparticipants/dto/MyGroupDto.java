package kr.ai.nemo.groupparticipants.dto;

import java.util.List;
import kr.ai.nemo.group.domain.Group;

public record MyGroupDto(
    Long id,
    String name,
    String summary,
    String location,
    String imageUrl,
    int currentUserCount,
    int maxUserCount,
    String category,
    List<String> tags
) {
  public static MyGroupDto from(Group group) {
    return new MyGroupDto(
        group.getId(),
        group.getName(),
        group.getSummary(),
        group.getLocation(),
        group.getImageUrl(),
        group.getCurrentUserCount(),
        group.getMaxUserCount(),
        group.getCategory(),
        group.getGroupTags().stream()
            .map(gt -> gt.getTag().getName())
            .toList()
    );
  }
}
