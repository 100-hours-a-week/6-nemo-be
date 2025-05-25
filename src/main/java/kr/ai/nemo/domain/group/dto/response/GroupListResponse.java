package kr.ai.nemo.domain.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(name = "모임 리스트 조회 응답", description = "모임 리스트 조회 응답 DTO")
public record GroupListResponse(

    @Schema(description = "모임 목록")
    List<GroupDto> groups,

    @Schema(description = "전체 페이지 수", example = "5")
    int totalPages,

    @Schema(description = "전체 요소 수", example = "42")
    long totalElements,

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    int pageNumber,

    @Schema(description = "마지막 페이지 여부", example = "false")
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
