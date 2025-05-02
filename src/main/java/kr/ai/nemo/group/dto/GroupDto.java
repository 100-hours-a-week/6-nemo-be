package kr.ai.nemo.group.dto;

import java.util.List;
import kr.ai.nemo.group.domain.GroupTag;
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
  private List<GroupTag> tags;
}