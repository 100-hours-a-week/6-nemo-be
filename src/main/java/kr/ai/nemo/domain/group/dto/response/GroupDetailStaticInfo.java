package kr.ai.nemo.domain.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;

@Schema(name = "모임 상세 정적 정보", description = "변하지 않는 모임 정보")
public record GroupDetailStaticInfo(
    @Schema(description = "모임 이름")
    String name,
    
    @Schema(description = "모임 카테고리")
    String category,
    
    @Schema(description = "모임 요약")
    String summary,
    
    @Schema(description = "모임 설명")
    String description,
    
    @Schema(description = "추천 학습 계획")
    String plan,
    
    @Schema(description = "모임 장소")
    String location,

    @Schema(description = "현재 참여 인원")
    int currentUserCount,
    
    @Schema(description = "최대 참여 인원")
    int maxUserCount,
    
    @Schema(description = "모임 이미지 URL")
    String imageUrl,
    
    @Schema(description = "모임 태그 목록")
    List<String> tags,
    
    @Schema(description = "모임장 닉네임")
    String ownerName
) {
    public static GroupDetailStaticInfo from(Group group, List<String> tags) {
        return new GroupDetailStaticInfo(
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
