package kr.ai.nemo.schedule.dto;

import java.time.format.DateTimeFormatter;
import kr.ai.nemo.schedule.domain.Schedule;

public record ScheduleCreateResponse(
    Long id,
    String title,
    String startAt,
    String address,
    Long ownerId,
    String createdAt,
    String description,
    GroupInfo group
) {
  public static ScheduleCreateResponse from(Schedule schedule) {
    return new ScheduleCreateResponse(
        schedule.getId(),
        schedule.getTitle(),
        schedule.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        schedule.getAddress(),
        schedule.getOwner().getId(),
        schedule.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        schedule.getDescription(),
        new GroupInfo(
            schedule.getGroup().getId(),
            schedule.getGroup().getName(),
            schedule.getGroup().getCurrentUserCount()
        )
    );
  }

  public record GroupInfo(Long groupId, String name, int currentUser) {}
}
