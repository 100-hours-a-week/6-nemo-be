package kr.ai.nemo.domain.groupparticipants.dto.response;

import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "나의 모임 리스트 내 모임 정보", description = "나의 모임 리스트 응답 DTO")
public record MyGroupDto(
    @Schema(description = "모임 ID", example = "32")
    Long groupId,

    @Schema(description = "모임 이름", example = "카테부 런닝")
    String name,

    @Schema(description = "모임 카테고리", example = "스포츠")
    String category,

    @Schema(description = "모임 요약", example = "5km 마라톤 완주를 목표로 하는 즐거운 달리기 모임입니다.")
    String summary,

    @Schema(description = "모임 위치", example = "경기 성남시 분당구 대왕판교로 660 금토천교")
    String location,

    @Schema(description = "현재 참여 인원", example = "5")
    int currentUserCount,

    @Schema(description = "최대 참여 인원", example = "49")
    int maxUserCount,

    @Schema(description = "모임 이미지 URL", example = "https://nemo-uploaded-files.s3.ap-northeast-2.amazonaws.com/groups/profile_beca74ec-7411-497f-b3c4-dd577a43a6a9.jpg")
    String imageUrl,

    @Schema(description = "모임 태그 목록", example = "[\"마라톤\", \"스포츠\", \"모임\", \"훈련\",\"번개런\",\"3키로\"]")
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
