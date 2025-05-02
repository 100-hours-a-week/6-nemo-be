package kr.ai.nemo.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.stream.Collectors;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.domain.enums.Category;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(Include.NON_EMPTY)
public class GroupDetailResponse {
  private String name;
  private String summary;
  private String description;
  private Category category;
  private String location;
  private int currentUserCount;
  private int maxUserCount;
  private String imageUrl;
  private List<String> tags;
  private String plan;

  public static GroupDetailResponse from(Group group) {
    return new GroupDetailResponse(
        group.getName(),
        group.getSummary(),
        group.getDescription(),
        group.getCategory(),
        group.getLocation(),
        group.getCurrentUserCount(),
        group.getMaxUserCount(),
        group.getImageUrl(),
        group.getGroupTags().stream()
            .map(groupTag -> groupTag.getTag().getName())
            .collect(Collectors.toList()),
        group.getPlan()
    );
  }
}
