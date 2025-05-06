package kr.ai.nemo.group.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import kr.ai.nemo.group.domain.enums.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class GroupSearchRequest {

  private String category;

  @Size(min = 2, max = 64)
  private String keyword;

  @Min(0)
  private int page = 0;

  @Min(1)
  @Max(100)
  private int size = 10;

  private String sort = "createdAt";
  
  private String direction = "desc";

  public Category getCategoryEnum() {
    if (this.category == null || this.category.isBlank()) {
      return null;
    }

    return Category.fromDisplayName(this.category);
  }
}