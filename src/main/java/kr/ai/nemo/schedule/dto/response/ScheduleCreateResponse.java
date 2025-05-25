package kr.ai.nemo.schedule.dto.response;

import java.time.format.DateTimeFormatter;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.domain.enums.ScheduleStatus;

public record ScheduleCreateResponse(
    Long scheduleId,
    String title,
    String description,
    String address,
    ScheduleStatus status,
    int currentUserCount,
    String ownerName,
    String startAt,
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

  public record GroupInfo(Long groupId, String name, int currentUser) {}
}
