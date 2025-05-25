package kr.ai.nemo.schedule.dto.request;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import jakarta.validation.constraints.Min;

public record PageRequestDto(
    @Min(0) int page,
    @Min(1) int size,
    String sort,
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
