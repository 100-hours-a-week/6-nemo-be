package kr.ai.nemo.global.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import jakarta.validation.constraints.Min;

@Schema(name = "오프셋 페이징", description = "오프셋 페이징 DTO")
public record PageRequestDto(
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    @Min(0) int page,

    @Schema(description = "페이지 당 요소 개수", example = "10")
    @Min(1) @Max(100) int size,

    @Schema(description = "정렬 기준 필드명", example = "createdAt")
    String sort,

    @Schema(description = "정렬 방향 (asc 또는 desc)", example = "desc")
    String direction
) {
  public PageRequest toPageRequest(String defaultSort, String defaultDirection) {
    String finalSort = (sort == null || sort.isBlank()) ? defaultSort : sort;
    String finalDirection = (direction == null || direction.isBlank()) ? defaultDirection : direction;

    return PageRequest.of(
        page,
        size,
        Sort.by(Sort.Direction.fromString(finalDirection), finalSort)
    );
  }
}
