package kr.ai.nemo.domain.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(name = "모임 생성 요청", description = "모임 생성 요청 DTO")
public record GroupCreateRequest(

    @Schema(description = "모임 이름", example = "백엔드 스터디")
    @NotBlank(message = "{group.name.notBlank}")
    @Size(min = 1, max = 64, message = "{group.name.size}")
    String name,

    @Schema(description = "모임 요약", example = "Spring 입문자들을 위한 스터디입니다.")
    @NotBlank(message = "{group.summary.notBlank}")
    String summary,

    @Schema(description = "모임 설명", example = "이 모임은 Spring Boot와 JPA를 함께 공부하는 스터디입니다.")
    @NotBlank(message = "{group.description.notBlank}")
    @Size(min = 1, max = 1000, message = "{group.description.size}")
    String description,

    @Schema(description = "카테고리", example = "IT/개발")
    @NotNull(message = "{group.category.notNull}")
    String category,

    @Schema(description = "모임 지역", example = "서울 강남구")
    @NotBlank(message = "{group.location.notBlank}")
    String location,

    @Schema(description = "최대 인원 수", example = "10")
    @Positive(message = "{group.minUserCount.min}")
    @Max(value = 100, message = "{group.minUserCount.max}")
    int maxUserCount,

    @Schema(description = "대표 이미지 URL", example = "https://example.com/image.png")
    String imageUrl,

    @Schema(description = "태그 목록", example = "[\"Spring\", \"JPA\"]")
    @NotEmpty(message = "{group.tags.notEmpty}")
    List<String> tags,

    @Schema(description = "추천 학습 계획", example = "1주차: 스프링 입문, 2주차: JPA 기초")
    String plan
) {}
