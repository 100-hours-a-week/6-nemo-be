package kr.ai.nemo.schedule.dto;

import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.participants.domain.ScheduleParticipant;

public record ScheduleDetailResponse(
    Long id,
    String title,
    String startAt,
    String address,
    String ownerName,
    String createdAt,
    String description,
    String scheduleStatus,
    GroupInfo group,
    List<ParticipantInfo> participants
) {
  public record GroupInfo(Long groupId, String name, int currentUserCount, int maxUserCount) {}

  public record ParticipantInfo(Long id, UserInfo user, String status) {}

  public record UserInfo(Long userId, String nickname, String profileImage) {}

  public static ScheduleDetailResponse from(Schedule schedule, List<ScheduleParticipant> participants) {
    return new ScheduleDetailResponse(
        schedule.getId(),
        schedule.getTitle(),
        schedule.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        schedule.getAddress(),
        schedule.getOwner().getNickname(),
        schedule.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        schedule.getDescription(),
        schedule.getStatus().name(),
        new GroupInfo(
            schedule.getGroup().getId(),
            schedule.getGroup().getName(),
            schedule.getGroup().getCurrentUserCount(),
            schedule.getGroup().getMaxUserCount()
        ),
        participants.stream()
            .map(p -> new ParticipantInfo(
                p.getId(),
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
