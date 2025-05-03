package kr.ai.nemo.user.dto;

import kr.ai.nemo.user.domain.User;
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
  private int cacheTtl;

  public static UserDto from(User user) {
    return UserDto.builder()
        .userId(user.getId())
        .nickname(user.getNickname())
        .profileImageUrl(user.getProfileImageUrl())
        .cacheTtl(300)
        .build();
  }
}
