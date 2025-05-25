package kr.ai.nemo.domain.schedule.dto.response;

import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일정 상세 조회 응답 DTO")
public record ScheduleDetailResponse(
    @Schema(description = "일정 ID", example = "1001")
    Long scheduleId,

    @Schema(description = "일정 제목", example = "주간 미팅")
    String title,

    @Schema(description = "일정 상세 설명", example = "프로젝트 진행 상황 점검")
    String description,

    @Schema(description = "일정 장소 주소", example = "서울시 강남구")
    String address,

    @Schema(description = "일정 상태", example = "ONGOING")
    String status,

    @Schema(description = "일정 주최자 이름", example = "홍길동")
    String ownerName,

    @Schema(description = "일정 시작 시각 (yyyy-MM-dd HH:mm)", example = "2025-05-25 14:00")
    String startAt,

    @Schema(description = "일정 생성 시각 (yyyy-MM-dd HH:mm)", example = "2025-05-01 09:00")
    String createdAt,

    @Schema(description = "소속 그룹 정보")
    GroupInfo group,

    @Schema(description = "참여자 목록")
    List<ParticipantInfo> participants
) {
  @Schema(description = "그룹 정보")
  public record GroupInfo(
      @Schema(description = "그룹 ID", example = "200")
      Long groupId,

      @Schema(description = "그룹 이름", example = "개발팀")
      String name,

      @Schema(description = "현재 그룹 인원 수", example = "10")
      int currentUserCount,

      @Schema(description = "그룹 최대 인원 수", example = "20")
      int maxUserCount
  ) {}

  @Schema(description = "참여자 정보")
  public record ParticipantInfo(
      @Schema(description = "사용자 정보")
      UserInfo user,

      @Schema(description = "참여 상태", example = "ACCEPTED")
      String status
  ) {}

  @Schema(description = "사용자 정보")
  public record UserInfo(
      @Schema(description = "사용자 ID", example = "501")
      Long userId,

      @Schema(description = "사용자 닉네임", example = "user123")
      String nickname,

      @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile/user123.png")
      String profileImageUrl
  ) {}

  public static ScheduleDetailResponse from(Schedule schedule, List<ScheduleParticipant> participants) {
    return new ScheduleDetailResponse(
        schedule.getId(),
        schedule.getTitle(),
        schedule.getDescription(),
        schedule.getAddress(),
        schedule.getStatus().name(),
        schedule.getOwner().getNickname(),
        schedule.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        schedule.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        new GroupInfo(
            schedule.getGroup().getId(),
            schedule.getGroup().getName(),
            schedule.getGroup().getCurrentUserCount(),
            schedule.getGroup().getMaxUserCount()
        ),
        participants.stream()
            .map(p -> new ParticipantInfo(
                new UserInfo(
                    p.getUser().getId(),
                    p.getUser().getNickname(),
                    p.getUser().getProfileImageUrl()
                ),
                p.getStatus().name()
            ))
            .toList()
    );
  }
}
