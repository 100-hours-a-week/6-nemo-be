package kr.ai.nemo.domain.group.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;

@JsonInclude(Include.NON_EMPTY)
public record GroupDetailResponse(

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

    @Schema(description = "모임 장소", example = "서울 강남구")
    String location,

    @Schema(description = "현재 참여 인원", example = "5")
    int currentUserCount,

    @Schema(description = "최대 참여 인원", example = "10")
    int maxUserCount,

    @Schema(description = "모임 이미지 URL", example = "https://example.com/image.jpg")
    String imageUrl,

    @Schema(description = "모임 태그 목록", example = "[\"Spring\", \"JPA\", \"Docker\"]")
    List<String> tags,

    @Schema(description = "모임장 닉네임", example = "admin")
    String ownerName
) {

  public static GroupDetailResponse from(Group group, List<String> tags) {
    return new GroupDetailResponse(
        group.getName(),
        group.getCategory(),
        group.getSummary(),
        group.getDescription(),
        group.getPlan(),
        group.getLocation(),
        group.getCurrentUserCount(),
        group.getMaxUserCount(),
        group.getImageUrl(),
        tags,
        group.getOwner().getNickname()
    );
  }
}
