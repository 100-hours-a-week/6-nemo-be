package kr.ai.nemo.global.fixture.schedule;

import java.time.LocalDateTime;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.user.domain.User;

public class ScheduleFixture {
  public static Schedule createSchedule(Group group, User user, ScheduleStatus scheduleStatus) {
    return Schedule.builder()
        .group(group)
        .owner(user)
        .title("test")
        .description("test입니다.")
        .address("서울 강남구")
        .currentUserCount(1)
        .status(scheduleStatus)
        .startAt(LocalDateTime.now())
        .build();
  }

  public static Schedule createDefaultSchedule(Group group, User user, ScheduleStatus scheduleStatus) {
    return createSchedule(group, user, scheduleStatus);
  }

}
