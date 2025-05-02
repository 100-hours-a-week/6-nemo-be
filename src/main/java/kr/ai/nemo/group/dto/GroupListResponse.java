package kr.ai.nemo.group.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupListResponse {
  private List<GroupDto> groups;
  private int totalCount;
  private int page;
  private int size;

  public static GroupListResponse from(List<GroupDto> groups, int totalCount, int page, int size) {
    return GroupListResponse.builder()
        .groups(groups)
        .totalCount(totalCount)
        .page(page)
        .size(size)
        .build();
  }
}