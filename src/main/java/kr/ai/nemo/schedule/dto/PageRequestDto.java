package kr.ai.nemo.schedule.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import jakarta.validation.constraints.Min;

public record PageRequestDto(
    @Min(0) int page,
    @Min(1) int size,
    String sort,
    String direction
) {
  public PageRequestDto {
    if (sort == null || sort.isBlank()) sort = "createdAt";
    if (direction == null || direction.isBlank()) direction = "desc";
  }

  public PageRequest toPageRequest() {
    return PageRequest.of(
        page,
        size,
        Sort.by(Sort.Direction.fromString(direction), sort)
    );
  }
}
