package kr.ai.nemo.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ScheduleCreateRequest(
    @NotNull Long groupId,
    @NotBlank String title,
    @NotBlank String description,
    @NotBlank String address,
    String addressDetail,
    @NotNull LocalDateTime startAt


) {
  public String fullAddress() {
    return addressDetail == null || addressDetail.isBlank()
        ? address
        : address + ", " + addressDetail;
  }
}
