package kr.ai.nemo.domain.group.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GroupAiGenerateRequest (

  @NotBlank(message = "모임명은 필수입니다.")
  String name,

  @NotBlank(message = "모임 목표는 필수입니다.")
  String goal,

  @NotNull(message = "모임 카테고리는 필수입니다.")
  String category,

  @NotBlank(message = "기간 선택은 필수입니다.")
  String period,

  @JsonProperty("isPlanCreated")
  @NotNull(message = "학습계획 생성 여부 선택은 필수입니다.")
  boolean isPlanCreated
) {
  public static GroupAiGenerateRequest from(GroupGenerateRequest req) {
    return new GroupAiGenerateRequest(
        req.name(),
        req.goal(),
        req.category(),
        req.period(),
        req.isPlanCreated()
    );
  }
}
