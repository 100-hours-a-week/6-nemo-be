package kr.ai.nemo.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScheduleStatusUpdater {

  @Scheduled(cron = "0 0/5 * * * *")
  public void updateClosedSchedules() {
    log.info("Scheduler Status Updater: Updating closed schedules");
  }
}
