package kr.ai.nemo.schedule.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleStatusUpdater {

  private final ScheduleRepository scheduleRepository;

  @Scheduled(cron = "0 0/5 * * * *")
  @Transactional
  public void updateClosedSchedules() {
    LocalDateTime now = LocalDateTime.now();
    log.info("[Scheduler] 일정 상태 업데이트 실행 at {}", now);

    List<Schedule> outdatedSchedules = scheduleRepository.findByStartAtBeforeAndStatus(now, ScheduleStatus.RECRUITING);

    for (Schedule schedule : outdatedSchedules) {
      schedule.complete();
      schedule.getGroup().addCompleteSchedule();
      log.info("일정 상태 변경: [{}] → CLOSED", schedule.getId());
    }
  }
}
