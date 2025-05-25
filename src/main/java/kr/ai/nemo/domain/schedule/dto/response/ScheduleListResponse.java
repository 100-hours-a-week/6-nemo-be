package kr.ai.nemo.domain.schedule.dto.response;

import java.util.List;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;

public record ScheduleListResponse(
    List<ScheduleSummary> schedules,
    int totalPages,
    long totalElements,
    int pageNumber,
    boolean isLast
) {
  public record ScheduleSummary(
      Long scheduleId,
      String title,
      String description,
      String address,
      ScheduleStatus status,
      int currentUserCount,
      String ownerName,
      String startAt,
      String createdAt
  ) {}
}
