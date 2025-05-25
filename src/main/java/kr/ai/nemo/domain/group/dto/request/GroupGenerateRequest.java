package kr.ai.nemo.domain.group.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "모임 정보 생성 요청", description = "모임 정보 생성 요청 DTO")
public record GroupGenerateRequest(

    @Schema(description = "모임 이름", example = "백엔드 스터디")
    @NotBlank(message = "{group.name.notBlank}")
    @Size(min = 1, max = 64, message = "{group.name.size}")
    String name,

    @Schema(description = "모임 목표", example = "Spring 완전 정복")
    @Size(min = 1, max = 255, message = "{group.goal.size}")
    String goal,

    @Schema(description = "모임 카테고리", example = "IT/개발")
    @NotNull(message = "{group.category.notNull}")
    String category,

    @Schema(description = "모임 위치", example = "서울특별시 강남구")
    @NotBlank(message = "{group.location.notBlank}")
    String location,

    @Schema(description = "모임 기간", example = "8주")
    @NotBlank(message = "{group.period.notBlank}")
    String period,

    @Schema(description = "최대 인원 수", example = "10")
    @Min(value = 2, message = "{group.maxUserCount.min}")
    @Max(value = 100, message = "{group.maxUserCount.max}")
    int maxUserCount,

    @Schema(description = "AI 계획 생성 희망 여부", example = "true")
    boolean isPlanCreated
) {}
