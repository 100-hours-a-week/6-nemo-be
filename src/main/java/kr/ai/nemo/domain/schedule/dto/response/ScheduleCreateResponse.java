package kr.ai.nemo.domain.schedule.dto.response;

import java.time.format.DateTimeFormatter;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "일정 생성 응답", description = "일정 생성 응답 DTO")
public record ScheduleCreateResponse(
    @Schema(description = "일정 ID", example = "55")
    Long scheduleId,

    @Schema(description = "일정 제목", example = "주간 미팅")
    String title,

    @Schema(description = "일정 상세 내용", example = "이번 주 진행할 업무 점검")
    String description,

    @Schema(description = "일정 주소", example = "서울특별시 강남구, 역삼동 123-45")
    String address,

    @Schema(description = "일정 상태", example = "RECRUITING")
    ScheduleStatus status,

    @Schema(description = "현재 참여 인원", example = "10")
    int currentUserCount,

    @Schema(description = "일정 주최자 이름", example = "전상현(Sanghyun Jun)")
    String ownerName,

    @Schema(description = "일정 시작 시각 (yyyy-MM-dd HH:mm)", example = "2025-05-25 14:00")
    String startAt,

    @Schema(description = "일정 생성 시각 (yyyy-MM-dd HH:mm)", example = "2025-05-01 09:00")
    String createdAt
) {
  public static ScheduleCreateResponse from(Schedule schedule) {
    return new ScheduleCreateResponse(
        schedule.getId(),
        schedule.getTitle(),
        schedule.getDescription(),
        schedule.getAddress(),
        schedule.getStatus(),
        schedule.getCurrentUserCount(),
        schedule.getOwner().getNickname(),
        schedule.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        schedule.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    );
  }

  @Schema(description = "그룹 정보")
  public record GroupInfo(
      @Schema(description = "그룹 ID", example = "200")
      Long groupId,

      @Schema(description = "그룹 이름", example = "개발팀")
      String name,

      @Schema(description = "현재 그룹 인원 수", example = "10")
      int currentUser
  ) {}
}
