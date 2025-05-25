package kr.ai.nemo.domain.schedule.dto.response;

import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;

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
      String title,
      String description,
      String address,
      ScheduleStatus status,
      int currentUserCount,
      String groupName,
      String ownerName,
      String startAt

  ) {
    public static ScheduleInfo from(ScheduleParticipant participant) {
      Schedule schedule = participant.getSchedule();
      return new ScheduleInfo(
          schedule.getId(),
          schedule.getTitle(),
          schedule.getDescription(),
          schedule.getAddress(),
          schedule.getStatus(),
          schedule.getCurrentUserCount(),
          schedule.getGroup().getName(),
          schedule.getOwner().getNickname(),
          schedule.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
      );
    }
  }
}
