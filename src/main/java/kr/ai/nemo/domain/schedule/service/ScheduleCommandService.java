package kr.ai.nemo.domain.schedule.service;

import java.time.LocalDateTime;
import kr.ai.nemo.global.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.dto.request.ScheduleCreateRequest;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleCreateResponse;
import kr.ai.nemo.domain.schedule.validator.ScheduleValidator;
import kr.ai.nemo.domain.scheduleparticipants.service.ScheduleParticipantsService;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleCommandService {
  private final ScheduleRepository scheduleRepository;
  private final ScheduleParticipantsService scheduleParticipantsService;
  private final GroupValidator groupValidator;
  private final ScheduleValidator scheduleValidator;

  @CacheEvict(value = "group-list", allEntries = true)
  @TimeTrace
  @Transactional
  public ScheduleCreateResponse createSchedule(CustomUserDetails userDetails, ScheduleCreateRequest request){
    Group group = groupValidator.findByIdOrThrow(request.groupId());

    Schedule schedule = Schedule.builder()
        .group(group)
        .owner(userDetails.getUser())
        .title(request.title())
        .description(request.description())
        .address(request.fullAddress())
        .currentUserCount(1)
        .status(ScheduleStatus.RECRUITING)
        .startAt(request.startAt())
        .build();

    scheduleRepository.save(schedule);
    group.setUpdatedAt(LocalDateTime.now());
    scheduleParticipantsService.addAllParticipantsForNewSchedule(schedule);
    return ScheduleCreateResponse.from(schedule);
  }

  @Transactional
  public void deleteSchedule(Long scheduleId) {
    Schedule schedule = scheduleValidator.findByIdOrThrow(scheduleId);
    scheduleValidator.validateSchedule(schedule);

    schedule.cancel();
  }
}
