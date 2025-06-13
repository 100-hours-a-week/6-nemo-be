package kr.ai.nemo.domain.group.dto.response;

import java.util.List;
import java.util.stream.Collectors;
import kr.ai.nemo.domain.group.domain.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDto {

  private Long groupId;
  private String name;
  private String category;
  private String summary;
  private String location;
  private int currentUserCount;
  private int maxUserCount;
  private String imageUrl;
  private List<String> tags;

  public static GroupDto from(Group group) {
    List<String> tags = group.getGroupTags().stream()
        .map(tag -> tag.getTag().getName())
        .collect(Collectors.toList());

    return GroupDto.builder()
        .groupId(group.getId())
        .name(group.getName())
        .category(group.getCategory())
        .summary(group.getSummary())
        .location(group.getLocation())
        .currentUserCount(group.getCurrentUserCount())
        .maxUserCount(group.getMaxUserCount())
        .imageUrl(group.getImageUrl())
        .tags(tags)
        .build();
  }
}
