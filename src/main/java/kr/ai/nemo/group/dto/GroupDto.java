package kr.ai.nemo.group.dto;

import java.util.List;
import java.util.stream.Collectors;
import kr.ai.nemo.group.domain.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDto {
  private Long id;
  private String name;
  private String summary;
  private String location;
  private int currentUserCount;
  private int maxUserCount;
  private String category;
  private List<String> tags;


  public static GroupDto from(Group group) {
    List<String> tags = group.getGroupTags().stream()
        .map(tag -> tag.getTag().getName())
        .collect(Collectors.toList());

    return GroupDto.builder()
        .id(group.getId())
        .name(group.getName())
        .summary(group.getSummary())
        .location(group.getLocation())
        .currentUserCount(group.getCurrentUserCount())
        .maxUserCount(group.getMaxUserCount())
        .category(group.getCategory().name())
        .tags(tags)
        .build();
  }
}