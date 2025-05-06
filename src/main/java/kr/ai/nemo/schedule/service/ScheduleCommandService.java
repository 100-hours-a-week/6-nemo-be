package kr.ai.nemo.schedule.service;

import java.time.LocalDateTime;
import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.participants.service.GroupParticipantsService;
import kr.ai.nemo.group.service.GroupQueryService;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.schedule.dto.ScheduleCreateRequest;
import kr.ai.nemo.schedule.dto.ScheduleCreateResponse;
import kr.ai.nemo.schedule.participants.service.ScheduleParticipantsService;
import kr.ai.nemo.schedule.repository.ScheduleRepository;
import kr.ai.nemo.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleCommandService {
  private final ScheduleRepository scheduleRepository;
  private final GroupQueryService groupQueryService;
  private final UserQueryService userQueryService;
  private final GroupParticipantsService groupParticipantsService;
  private final ScheduleParticipantsService scheduleParticipantsService;

  @Transactional
  public ScheduleCreateResponse createSchedule(Long userId, ScheduleCreateRequest request){
    groupParticipantsService.validateJoinedParticipant(request.groupId(), userId);
    Group group = groupQueryService.findByIdOrThrow(request.groupId());
    Schedule schedule = Schedule.builder()
        .group(group)
        .owner(userQueryService.findByIdOrThrow(userId))
        .title(request.title())
        .description(request.description())
        .address(request.fullAddress())
        .currentUserCount(1)
        .status(ScheduleStatus.RECRUITING)
        .startAt(request.startAt())
        .build();

    scheduleRepository.save(schedule);
    scheduleParticipantsService.addAllParticipantsForNewSchedule(schedule);
    return ScheduleCreateResponse.from(schedule);
  }

  @Transactional
  public void deleteSchedule(Long userId, Long scheduleId) {
    Schedule schedule = findByIdOrThrow(scheduleId);

    if (schedule.getStatus() == ScheduleStatus.CLOSED) {
      throw new CustomException(ResponseCode.SCHEDULE_ALREADY_ENDED);
    } else if (schedule.getStatus() == ScheduleStatus.CANCELED) {
      throw new CustomException(ResponseCode.SCHEDULE_ALREADY_CANCELED);
    }

    if (!schedule.getOwner().getId().equals(userId)) {
      throw new CustomException(ResponseCode.SCHEDULE_DELETE_FORBIDDEN);
    }

    schedule.cancel();
  }

  public Schedule findByIdOrThrow(Long scheduleId) {
    return scheduleRepository.findByIdAndStatusNot(scheduleId, ScheduleStatus.CANCELED)
        .orElseThrow(() -> new CustomException(ResponseCode.SCHEDULE_NOT_FOUND));
  }
}
