package kr.ai.nemo.domain.user.dto;

import kr.ai.nemo.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
  private Long userId;
  private String nickname;
  private String profileImageUrl;

  public static UserDto from(User user) {
    return UserDto.builder()
        .userId(user.getId())
        .nickname(user.getNickname())
        .profileImageUrl(user.getProfileImageUrl())
        .build();
  }
}
