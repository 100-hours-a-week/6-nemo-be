package kr.ai.nemo.group.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record GroupCreateRequest(
    @NotBlank(message = "{group.name.notBlank}")
    @Size(min = 1, max = 64, message = "{group.name.size}")
    String name,

    @NotBlank(message = "{group.summary.notBlank}")
    String summary,

    @NotBlank(message = "{group.description.notBlank}")
    @Size(min = 1, max = 1000, message = "{group.description.size}")
    String description,

    @NotNull(message = "{group.category.notNull}")
    String category,

    @NotBlank(message = "{group.location.notBlank}")
    String location,

    @Positive(message = "{group.minUserCount.min}")
    @Max(value = 100, message = "{group.minUserCount.max}")
    int maxUserCount,

    String imageUrl,

    @NotEmpty(message = "{group.tags.notEmpty}")
    List<String> tags,

    String plan
) {}
