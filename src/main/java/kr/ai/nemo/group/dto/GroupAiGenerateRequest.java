package kr.ai.nemo.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupAiGenerateRequest {

  @NotBlank(message = "모임명은 필수입니다.")
  private String name;

  @NotBlank(message = "모임 목표는 필수입니다.")
  private String goal;

  @NotNull(message = "모임 카테고리는 필수입니다.")
  private String category;

  @NotBlank(message = "기간 선택은 필수입니다.")
  private String period;

  @NotNull(message = "학습계획 생성 여부 선택은 필수입니다.")
  private boolean isPlanCreated;
}