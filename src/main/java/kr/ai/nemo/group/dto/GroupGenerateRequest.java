package kr.ai.nemo.group.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GroupGenerateRequest(
    @NotBlank(message = "{group.name.notBlank}")
    @Size(min = 1, max = 64, message = "{group.name.size}")
    String name,

    @Size(min = 1, max = 255, message = "{group.goal.size}")
    String goal,

    @NotNull(message = "{group.category.notNull}")
    String category,

    @NotBlank(message = "{group.location.notBlank}")
    String location,

    @NotBlank(message = "{group.period.notBlank}")
    String period,

    @Min(value = 2, message = "{group.maxUserCount.min}")
    @Max(value = 100, message = "{group.maxUserCount.max}")
    int maxUserCount,

    boolean isPlanCreated
) {}
