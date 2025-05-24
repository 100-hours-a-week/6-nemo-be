package kr.ai.nemo.schedule.service;

import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.validator.GroupValidator;
import kr.ai.nemo.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.schedule.dto.ScheduleCreateRequest;
import kr.ai.nemo.schedule.dto.ScheduleCreateResponse;
import kr.ai.nemo.schedule.validator.ScheduleValidator;
import kr.ai.nemo.scheduleparticipants.service.ScheduleParticipantsService;
import kr.ai.nemo.schedule.repository.ScheduleRepository;
import kr.ai.nemo.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleCommandService {
  private final ScheduleRepository scheduleRepository;
  private final ScheduleParticipantsService scheduleParticipantsService;
  private final GroupValidator groupValidator;
  private final UserValidator userValidator;
  private final GroupParticipantValidator groupParticipantValidator;
  private final ScheduleValidator scheduleValidator;

  @Transactional
  public ScheduleCreateResponse createSchedule(Long userId, ScheduleCreateRequest request){
    groupParticipantValidator.validateJoinedParticipant(request.groupId(), userId);
    Group group = groupValidator.findByIdOrThrow(request.groupId());

    Schedule schedule = Schedule.builder()
        .group(group)
        .owner(userValidator.findByIdOrThrow(userId))
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
    Schedule schedule = scheduleValidator.findByIdOrThrow(scheduleId);
    scheduleValidator.validateSchedule(schedule.getStatus());
    scheduleValidator.validateScheduleOwner(userId, schedule);

    schedule.cancel();
  }
}
