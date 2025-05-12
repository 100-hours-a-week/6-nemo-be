package kr.ai.nemo.group.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import kr.ai.nemo.group.domain.Group;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(Include.NON_EMPTY)
public class GroupCreateResponse {
  private Long groupId;
  private String name;
  private String summary;
  private String description;
  private String category;
  private String location;
  private int currentUserCount;
  private int maxUserCount;
  private String imageUrl;
  private List<String> tags;
  private String plan;

  public GroupCreateResponse(Group group) {
    this.groupId = group.getId();
    this.name = group.getName();
    this.summary = group.getSummary();
    this.description = group.getDescription();
    this.category = group.getCategory();
    this.location = group.getLocation();
    this.currentUserCount = group.getCurrentUserCount();
    this.maxUserCount = group.getMaxUserCount();
    this.imageUrl = group.getImageUrl();
    this.tags = getTags();
    this.plan = group.getPlan();
  }
}
