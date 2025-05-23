package kr.ai.nemo.schedule.validator;

import java.time.LocalDateTime;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.schedule.exception.ScheduleErrorCode;
import kr.ai.nemo.schedule.exception.ScheduleException;
import kr.ai.nemo.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ScheduleValidator {

  private final ScheduleRepository repository;

  public Schedule findByIdOrThrow(Long scheduleId) {
    Schedule schedule = repository.findByIdWithGroupAndOwner(scheduleId)
        .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
    if(schedule.getStatus() == ScheduleStatus.CANCELED){
      throw new ScheduleException(ScheduleErrorCode.SCHEDULE_ALREADY_CANCELED);
    }
    return schedule;
  }

  public void validateSchedule(ScheduleStatus status) {
    if (status.equals(ScheduleStatus.CLOSED)) {
      throw new ScheduleException(ScheduleErrorCode.SCHEDULE_ALREADY_ENDED);
    } else if (status.equals(ScheduleStatus.CANCELED)) {
      throw new ScheduleException(ScheduleErrorCode.SCHEDULE_ALREADY_CANCELED);
    }
  }

  public void validateScheduleOwner(Long userId, Schedule schedule) {
    if(userId.equals(schedule.getOwner().getId())) {
      throw new ScheduleException(ScheduleErrorCode.SCHEDULE_DELETE_FORBIDDEN);
    }
  }

  public void validateScheduleStart(Schedule schedule) {
    if (schedule.getStartAt().isBefore(LocalDateTime.now())) {
      throw new ScheduleException(ScheduleErrorCode.SCHEDULE_ALREADY_STARTED_OR_ENDED);
    }
  }
}
