package kr.ai.nemo.schedule.dto;

import java.util.List;

public record ScheduleListResponse(
    List<ScheduleSummary> schedules,
    int totalPages,
    long totalElements,
    int pageNumber,
    boolean isLast
) {
  public record ScheduleSummary(
      Long id,
      String title,
      String startAt,
      String address,
      String description,
      String ownerName,
      String ScheduleStatus,
      int currentUserCount
  ) {}
}
