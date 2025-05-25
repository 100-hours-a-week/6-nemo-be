package kr.ai.nemo.domain.schedule.dto.response;

import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 일정 응답 DTO")
public record MySchedulesResponse(
    @Schema(description = "미응답 일정 목록")
    List<ScheduleParticipation> notResponded,
    @Schema(description = "응답했으며 진행중인 일정 목록")
    List<ScheduleParticipation> respondedOngoing,
    @Schema(description = "응답했으며 완료된 일정 목록")
    List<ScheduleParticipation> respondedCompleted
) {

  @Schema(description = "일정 참여 정보")
  public record ScheduleParticipation(
      @Schema(description = "일정 정보")
      ScheduleInfo schedule
  ) {
    public static ScheduleParticipation from(ScheduleParticipant participant) {
      return new ScheduleParticipation(ScheduleInfo.from(participant));
    }
  }

  @Schema(description = "일정 정보")
  public record ScheduleInfo(
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

      @Schema(description = "현재 참여 인원", example = "10")
      int currentUserCount,

      @Schema(description = "소속 그룹 이름", example = "개발팀")
      String groupName,

      @Schema(description = "일정 주최자 이름", example = "홍길동")
      String ownerName,

      @Schema(description = "일정 시작 시각 (yyyy-MM-dd HH:mm)", example = "2025-05-25 14:00")
      String startAt
  ) {
    public static ScheduleInfo from(ScheduleParticipant participant) {
      Schedule schedule = participant.getSchedule();
      return new ScheduleInfo(
          schedule.getId(),
          schedule.getTitle(),
          schedule.getDescription(),
          schedule.getAddress(),
          schedule.getStatus(),
          schedule.getCurrentUserCount(),
          schedule.getGroup().getName(),
          schedule.getOwner().getNickname(),
          schedule.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
      );
    }
  }
}
