package kr.ai.nemo.domain.schedule.dto.response;

import java.util.List;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "일정 리스트 조회 응답", description = "일정 리스트 응답 DTO")
public record ScheduleListResponse(
    @Schema(description = "일정 요약 목록")
    List<ScheduleSummary> schedules,

    @Schema(description = "총 페이지 수", example = "5")
    int totalPages,

    @Schema(description = "총 요소 개수", example = "100")
    long totalElements,

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    int pageNumber,

    @Schema(description = "마지막 페이지 여부", example = "false")
    boolean isLast
) {

  @Schema(description = "일정 요약 정보")
  public record ScheduleSummary(
      @Schema(description = "일정 ID", example = "1001")
      Long scheduleId,

      @Schema(description = "일정 제목", example = "주간 미팅")
      String title,

      @Schema(description = "일정 상세 설명", example = "프로젝트 진행 상황 점검")
      String description,

      @Schema(description = "일정 장소 주소", example = "서울시 강남구")
      String address,

      @Schema(description = "일정 상태", example = "ONGOING")
      ScheduleStatus status,

      @Schema(description = "현재 일정 참여자 수", example = "1")
      int currentUserCount,

      @Schema(description = "일정 생성자 이름", example = "daniel")
      String ownerName,

      @Schema(description = "일정 시작 시각 (yyyy-MM-dd HH:mm)", example = "2025-05-25 14:00")
      String startAt,

      @Schema(description = "일정 생성 시각 (yyyy-MM-dd HH:mm)", example = "2025-05-01 09:00")
      String createdAt
  ) {}
}
