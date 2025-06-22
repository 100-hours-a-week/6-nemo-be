package kr.ai.nemo.domain.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Schema(name = "나의 일정 리스트 조회 응답", description = "내 일정 응답 DTO")
public record MySchedulesResponse(
    @Schema(description = "진행전 미응답 목록")
    List<ScheduleParticipation> notResponded,

    @Schema(description = "진행전 응답 목록")
    List<ScheduleParticipation> respondedOngoing,

    @Schema(description = "진행전 거절 목록")
    List<ScheduleParticipation> respondedRejected
) {

  @Schema(description = "일정 참여 정보")
  public record ScheduleParticipation(
      @Schema(description = "일정 정보") ScheduleInfo schedule
  ) {
    public static ScheduleParticipation fromProjection(ScheduleInfoProjection projection) {
      return new ScheduleParticipation(ScheduleInfo.fromProjection(projection));
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
    public static ScheduleInfo fromProjection(ScheduleInfoProjection p) {
      return new ScheduleInfo(
          p.getScheduleId(),
          p.getTitle(),
          p.getDescription(),
          p.getAddress(),
          p.getStatus(),
          p.getCurrentUserCount(),
          p.getGroupId(),
          p.getGroupName(),
          p.getOwnerName(),
          p.getStartAt()
      );
    }
  }
}
