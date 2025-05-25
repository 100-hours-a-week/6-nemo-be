// GroupParticipantsResponse.java
package kr.ai.nemo.groupparticipants.dto.response;

import java.util.List;
import kr.ai.nemo.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.user.domain.User;

public record GroupParticipantsListResponse(
    List<GroupParticipantDto> participants
) {
  public record GroupParticipantDto(
      Long userId,
      String nickname,
      String profileImageUrl,
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
