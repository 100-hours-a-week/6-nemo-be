package kr.ai.nemo.schedule.dto;

import java.util.List;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.participants.domain.ScheduleParticipant;

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
      Long scheduleId,
      String groupName,
      String title,
      String address,
      int currentUserCount,
      String ownerName,
      String startDate,
      String status
  ) {
    public static ScheduleInfo from(ScheduleParticipant participant) {
      Schedule schedule = participant.getSchedule();
      return new ScheduleInfo(
          schedule.getId(),
          schedule.getGroup().getName(),
          schedule.getTitle(),
          schedule.getAddress(),
          schedule.getCurrentUserCount(),
          schedule.getOwner().getNickname(),
          schedule.getStartAt().toLocalDate().toString(),
          participant.getStatus().name()
      );
    }
  }
}
