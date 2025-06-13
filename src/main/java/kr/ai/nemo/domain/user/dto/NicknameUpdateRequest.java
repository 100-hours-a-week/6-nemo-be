package kr.ai.nemo.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameUpdateRequest(
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20)
    String nickname
) {

}
