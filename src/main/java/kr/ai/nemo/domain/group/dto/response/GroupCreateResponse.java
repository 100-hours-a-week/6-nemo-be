package kr.ai.nemo.domain.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;

@Schema(name = "모임 생성 응답", description = "모임 생성 응답 DTO")
public record GroupCreateResponse(

    @Schema(description = "모임 ID", example = "1")
    Long groupId,

    @Schema(description = "모임 이름", example = "백엔드 스터디")
    String name,

    @Schema(description = "모임 카테고리", example = "IT/개발")
    String category,

    @Schema(description = "모임 요약", example = "Spring 학습에 집중하는 모임입니다.")
    String summary,

    @Schema(description = "모임 설명", example = "이 모임은 Spring Boot, JPA 등을 함께 공부하는 스터디입니다.")
    String description,

    @Schema(description = "추천 학습 계획", example = "1주차: 스프링 입문, 2주차: JPA 기초, ...")
    String plan,

    @Schema(description = "모임 위치", example = "서울 강남구")
    String location,

    @Schema(description = "현재 참여 인원 수", example = "1")
    int currentUserCount,

    @Schema(description = "최대 참여 인원 수", example = "10")
    int maxUserCount,

    @Schema(description = "모임 대표 이미지 URL", example = "https://example.com/group.png")
    String imageUrl,

    @Schema(description = "모임 태그 목록", example = "[\"Spring\", \"JPA\", \"Docker\"]")
    List<String> tags

) {
  public static GroupCreateResponse from(Group group, List<String> tags) {
    return new GroupCreateResponse(
        group.getId(),
        group.getName(),
        group.getCategory(),
        group.getSummary(),
        group.getDescription(),
        group.getPlan(),
        group.getLocation(),
        group.getCurrentUserCount(),
        group.getMaxUserCount(),
        group.getImageUrl(),
        tags
    );
  }
}
