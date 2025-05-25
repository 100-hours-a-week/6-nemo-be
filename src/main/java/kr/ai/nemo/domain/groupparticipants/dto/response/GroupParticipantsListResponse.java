// GroupParticipantsResponse.java
package kr.ai.nemo.domain.groupparticipants.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.user.domain.User;

public record GroupParticipantsListResponse(
    List<GroupParticipantDto> participants
) {
  public record GroupParticipantDto(
      @Schema(description = "사용자 ID", example = "12345")
      Long userId,

      @Schema(description = "사용자 닉네임", example = "user123")
      String nickname,

      @Schema(description = "사용자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
      String profileImageUrl,

      @Schema(description = "모임 내 사용자 역할 (LEADER/MEMBER)", example = "LEADER")
      String role
  ) {
    public static GroupParticipantDto from(GroupParticipants participants) {
      User user = participants.getUser();
      return new GroupParticipantDto(
          user.getId(),
          user.getNickname(),
          user.getProfileImageUrl(),
          participants.getRole().name()
      );
    }
  }
}
