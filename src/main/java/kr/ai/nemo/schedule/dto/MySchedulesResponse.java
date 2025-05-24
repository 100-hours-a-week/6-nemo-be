package kr.ai.nemo.schedule.dto;

import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.scheduleparticipants.domain.ScheduleParticipant;

public record MySchedulesResponse(
    List<ScheduleParticipation> notResponded,
    List<ScheduleParticipation> respondedOngoing,
    List<ScheduleParticipation> respondedCompleted
) {

  public record ScheduleParticipation(
      ScheduleInfo schedule
  ) {
    public static ScheduleParticipation from(ScheduleParticipant participant) {
      return new ScheduleParticipation(ScheduleInfo.from(participant));
    }
  }

  public record ScheduleInfo(
      Long id,
      String title,
      String address,
      String ownerName,
      String status,
      String groupName,
      int currentUserCount,
      String startAt

  ) {
    public static ScheduleInfo from(ScheduleParticipant participant) {
      Schedule schedule = participant.getSchedule();
      return new ScheduleInfo(
          schedule.getId(),
          schedule.getTitle(),
          schedule.getAddress(),
          schedule.getOwner().getNickname(),
          participant.getStatus().name(),
          schedule.getGroup().getName(),
          schedule.getCurrentUserCount(),
          schedule.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
      );
    }
  }
}
