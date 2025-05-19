package kr.ai.nemo.group.participants.dto;

import kr.ai.nemo.group.participants.domain.GroupParticipants;
import kr.ai.nemo.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupParticipantDto {
  private Long userId;
  private String nickname;
  private String profileImageUrl;
  private String role;

  public static GroupParticipantDto from(GroupParticipants participants) {
    User user = participants.getUser();
    return new GroupParticipantDto(user.getId(), user.getNickname(), user.getProfileImageUrl(), participants.getRole().name());
  }
}
