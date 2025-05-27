package kr.ai.nemo.domain.groupparticipants.dto.response;

import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "나의 모임 리스트 내 모임 정보", description = "나의 모임 리스트 응답 DTO")
public record MyGroupDto(
    @Schema(description = "모임 ID", example = "98765")
    Long groupId,

    @Schema(description = "모임 이름", example = "백엔드 스터디")
    String name,

    @Schema(description = "모임 카테고리", example = "IT/개발")
    String category,

    @Schema(description = "모임 요약", example = "Spring 집중 학습 모임")
    String summary,

    @Schema(description = "모임 위치", example = "서울 강남구")
    String location,

    @Schema(description = "현재 참여 인원", example = "7")
    int currentUserCount,

    @Schema(description = "최대 참여 인원", example = "10")
    int maxUserCount,

    @Schema(description = "모임 이미지 URL", example = "https://example.com/group-image.jpg")
    String imageUrl,

    @Schema(description = "모임 태그 목록", example = "[\"Spring\", \"JPA\", \"Docker\"]")
    List<String> tags
) {
  public static MyGroupDto from(Group group) {
    return new MyGroupDto(
        group.getId(),
        group.getName(),
        group.getCategory(),
        group.getSummary(),
        group.getLocation(),
        group.getCurrentUserCount(),
        group.getMaxUserCount(),
        group.getImageUrl(),
        group.getGroupTags().stream()
            .map(gt -> gt.getTag().getName())
            .toList()
    );
  }
}
