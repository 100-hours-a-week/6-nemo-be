package kr.ai.nemo.group.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record GroupListResponse(
    List<GroupDto> groups,
    int totalPages,
    long totalElements,
    int pageNumber,
    boolean isLast
) {
  public static GroupListResponse from(Page<GroupDto> page) {
    return new GroupListResponse(
        page.getContent(),
        page.getTotalPages(),
        page.getTotalElements(),
        page.getNumber(),
        page.isLast()
    );
  }
}
