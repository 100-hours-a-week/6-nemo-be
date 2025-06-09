package kr.ai.nemo.domain.schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "일정 생성 요청", description = "일정 생성 요청 DTO")
public record ScheduleCreateRequest(
    @Schema(description = "모임 ID", example = "32")
    @NotNull
    Long groupId,

    @Schema(description = "일정 제목", example = "주간 미팅")
    @NotBlank
    String title,

    @Schema(description = "일정 상세 내용", example = "이번 주 진행할 업무 점검")
    @NotBlank
    String description,

    @Schema(description = "주소", example = "서울특별시 강남구")
    @NotBlank
    String address,

    @Schema(description = "상세 주소", example = "역삼동 123-45")
    String addressDetail,

    @Schema(description = "일정 시작 시간", example = "2025-05-25T14:00")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm", timezone = "Asia/Seoul")
    LocalDateTime startAt
) {
  public String fullAddress() {
    return addressDetail == null || addressDetail.isBlank()
        ? address
        : address + ", " + addressDetail;
  }
}
