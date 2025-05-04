package kr.ai.nemo.group.participants.dto;

import kr.ai.nemo.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupParticipantDto {
  private Long userId;
  private String nickname;
  private String profileImageUrl;

  public static GroupParticipantDto from(User user) {
    return new GroupParticipantDto(user.getId(), user.getNickname(), user.getProfileImageUrl());
  }

}
