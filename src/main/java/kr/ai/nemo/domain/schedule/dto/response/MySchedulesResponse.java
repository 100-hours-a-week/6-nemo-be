package kr.ai.nemo.domain.schedule.dto.response;

import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "나의 일정 리스트 조회 응답", description = "내 일정 응답 DTO")
public record MySchedulesResponse(
    @Schema(description = "진행전 미응답 목록")
    List<ScheduleParticipation> notResponded,
    @Schema(description = "진행전 응답 목록")
    List<ScheduleParticipation> respondedOngoing
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
      @Schema(description = "일정 ID", example = "55")
      Long scheduleId,

      @Schema(description = "일정 제목", example = "판교역 베스킨라빈스31")
      String title,

      @Schema(description = "일정 상세 내용", example = "아이스크림~")
      String description,

      @Schema(description = "일정 주소", example = "경기 성남시 분당구 판교역로 166, 베스킨라빈스")
      String address,

      @Schema(description = "일정 상태", example = "RECRUITING")
      ScheduleStatus status,

      @Schema(description = "현재 참여 인원", example = "1")
      int currentUserCount,

      @Schema(description = "모임 ID", example = "35")
      Long groupId,

      @Schema(description = "소속 모임명", example = "로미의 백반기행")
      String groupName,

      @Schema(description = "일정 주최자 이름", example = "전상현(Sanghyun Jun)")
      String ownerName,

      @Schema(description = "일정 시작 시각 (yyyy-MM-dd HH:mm)", example = "2025-12-25 07:00")
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
          schedule.getGroup().getId(),
          schedule.getGroup().getName(),
          schedule.getOwner().getNickname(),
          schedule.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
      );
    }
  }
}
