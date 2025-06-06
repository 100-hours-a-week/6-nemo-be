package kr.ai.nemo.domain.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record NicknameUpdateRequest(
    @NotNull
    @Min(2) @Max(20)
    String nickname
) {

}
