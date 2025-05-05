package kr.ai.nemo.schedule.service;

import kr.ai.nemo.group.participants.service.GroupParticipantsService;
import kr.ai.nemo.group.service.GroupQueryService;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.schedule.dto.ScheduleCreateRequest;
import kr.ai.nemo.schedule.dto.ScheduleCreateResponse;
import kr.ai.nemo.schedule.repository.ScheduleRepository;
import kr.ai.nemo.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleCommandService {
  private final ScheduleRepository scheduleRepository;
  private final GroupQueryService groupQueryService;
  private final UserQueryService userQueryService;
  private final GroupParticipantsService groupParticipantsService;

  public ScheduleCreateResponse createSchedule(Long userId, ScheduleCreateRequest request){
    groupParticipantsService.validateJoinedParticipant(request.groupId(), userId);
    Schedule schedule = Schedule.builder()
        .group(groupQueryService.findByIdOrThrow(request.groupId()))
        .owner(userQueryService.findByIdOrThrow(userId))
        .title(request.title())
        .description(request.description())
        .address(request.fullAddress())
        .currentUserCount(1)
        .status(ScheduleStatus.RECRUITING)
        .startAt(request.startAt())
        .build();

    scheduleRepository.save(schedule);

    return ScheduleCreateResponse.from(schedule);
  }
}
