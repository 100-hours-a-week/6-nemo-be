package kr.ai.nemo.group.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import kr.ai.nemo.group.domain.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupCreateRequest {

  @NotBlank(message = "{group.name.notBlank}")
  @Size(min = 1, max = 64, message = "{group.name.size}")
  private String name;

  @NotBlank(message = "{group.summary.notBlank}")
  private String summary;

  @NotBlank(message = "{group.description.notBlank}")
  @Size(min = 1, max = 1000, message = "{group.description.size}")
  private String description;

  @NotNull(message = "{group.category.notNull}")
  private Category category;

  @NotBlank(message = "{group.location.notBlank}")
  private String location;

  @Positive(message = "{group.minUserCount.min}")
  @Max(value = 100, message = "{group.minUserCount.max}")
  private int maxUserCount;

  private String imageUrl;

  @NotEmpty(message = "{group.tags.notEmpty}")
  private List<String> tags;

  private String plan;
}
