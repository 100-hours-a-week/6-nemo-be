package kr.ai.nemo.domain.schedule.dto.response;

import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;

public record ScheduleDetailResponse(
    Long scheduleId,
    String title,
    String description,
    String address,
    String status,
    String ownerName,
    String startAt,
    String createdAt,
    GroupInfo group,
    List<ParticipantInfo> participants
) {
  public record GroupInfo(Long groupId, String name, int currentUserCount, int maxUserCount) {}

  public record ParticipantInfo(UserInfo user, String status) {}

  public record UserInfo(Long userId, String nickname, String profileImageUrl) {}

  public static ScheduleDetailResponse from(Schedule schedule, List<ScheduleParticipant> participants) {
    return new ScheduleDetailResponse(
        schedule.getId(),
        schedule.getTitle(),
        schedule.getAddress(),
        schedule.getOwner().getNickname(),
        schedule.getDescription(),
        schedule.getStatus().name(),
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
