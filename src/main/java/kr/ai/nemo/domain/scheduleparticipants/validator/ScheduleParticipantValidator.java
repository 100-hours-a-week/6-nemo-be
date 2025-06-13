package kr.ai.nemo.domain.scheduleparticipants.validator;

import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import kr.ai.nemo.domain.scheduleparticipants.domain.enums.ScheduleParticipantStatus;
import kr.ai.nemo.domain.scheduleparticipants.exception.ScheduleParticipantErrorCode;
import kr.ai.nemo.domain.scheduleparticipants.exception.ScheduleParticipantException;
import kr.ai.nemo.domain.scheduleparticipants.repository.ScheduleParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ScheduleParticipantValidator {

  private final ScheduleParticipantRepository repository;

  public ScheduleParticipant validateParticipationOrThrow(Long scheduleId, Long userId) {
    return repository.findByScheduleIdAndUserId(scheduleId, userId)
        .orElseThrow(() -> new ScheduleParticipantException(ScheduleParticipantErrorCode.NOT_GROUP_MEMBER));
  }

  public void validateStatusChange(ScheduleParticipantStatus currentStatus, ScheduleParticipantStatus newStatus) {
    if (currentStatus == newStatus) {
      throw new ScheduleParticipantException(ScheduleParticipantErrorCode.SCHEDULE_ALREADY_DECIDED);
    }
  }
}
