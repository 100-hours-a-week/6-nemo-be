package kr.ai.nemo.domain.group.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Schema(name = "모임 검색 요청", description = "모임 검색 요청 DTO")
public class GroupSearchRequest {

  @Schema(description = "검색할 모임 카테고리", example = "IT/개발")
  private String category;

  @Schema(description = "검색 키워드", example = "Spring")
  @Size(min = 2, max = 64)
  private String keyword;

  @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
  @Min(0)
  private int page = 0;

  @Schema(description = "페이지당 결과 수", example = "10")
  @Min(1)
  @Max(100)
  private int size = 10;

  @Schema(description = "정렬 기준 필드", example = "createdAt")
  private String sort = "createdAt";

  @Schema(description = "정렬 방향 (asc 또는 desc)", example = "desc")
  private String direction = "desc";
}
