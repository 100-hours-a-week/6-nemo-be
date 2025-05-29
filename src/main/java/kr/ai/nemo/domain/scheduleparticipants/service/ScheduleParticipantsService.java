package kr.ai.nemo.domain.scheduleparticipants.service;

import java.time.LocalDateTime;
import java.util.List;
import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.validator.ScheduleValidator;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import kr.ai.nemo.domain.scheduleparticipants.domain.enums.ScheduleParticipantStatus;
import kr.ai.nemo.domain.scheduleparticipants.repository.ScheduleParticipantRepository;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.scheduleparticipants.validator.ScheduleParticipantValidator;
import kr.ai.nemo.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleParticipantsService {
  private final ScheduleParticipantRepository scheduleParticipantRepository;
  private final ScheduleRepository scheduleRepository;
  private final ScheduleValidator scheduleValidator;
  private final ScheduleParticipantValidator scheduleParticipantValidator;

  @TimeTrace
  @Transactional
  public void addAllParticipantsForNewSchedule(Schedule schedule) {
    Group group = schedule.getGroup();

    List<User> users = group.getGroupParticipants().stream()
        .filter(p -> p.getStatus().isJoined())
        .map(GroupParticipants::getUser)
        .toList();

    for (User user : users) {
      ScheduleParticipantStatus status = user.getId().equals(schedule.getOwner().getId())
          ? ScheduleParticipantStatus.ACCEPTED
          : ScheduleParticipantStatus.PENDING;

      scheduleParticipantRepository.save(ScheduleParticipant.builder()
          .schedule(schedule)
          .user(user)
          .status(status)
          .build());
    }
  }

  @TimeTrace
  @Transactional
  public void addParticipantToUpcomingSchedules(Group group, User user) {
    List<Schedule> schedules = scheduleRepository.findByGroupAndStatus(group, ScheduleStatus.RECRUITING);

    for (Schedule schedule : schedules) {
      boolean exists = scheduleParticipantRepository.existsByScheduleAndUser(schedule, user);
      if (!exists) {
        ScheduleParticipant participant = ScheduleParticipant.builder()
            .schedule(schedule)
            .user(user)
            .status(ScheduleParticipantStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        scheduleParticipantRepository.save(participant);
      }
    }
  }

  @TimeTrace
  @Transactional
  public void decideParticipation(Long scheduleId, Long userId, ScheduleParticipantStatus status) {
    Schedule schedule = scheduleValidator.findByIdOrThrow(scheduleId);
    ScheduleParticipant participant = scheduleParticipantValidator.validateParticipationOrThrow(scheduleId, userId);
    scheduleValidator.validateScheduleStart(schedule);

    ScheduleParticipantStatus currentStatus = participant.getStatus();

    scheduleParticipantValidator.validateStatusChange(currentStatus, status);

    if (currentStatus == ScheduleParticipantStatus.ACCEPTED && status == ScheduleParticipantStatus.REJECTED) {
      schedule.subtractCurrentUserCount();
      participant.reject();
    } else if ((currentStatus == ScheduleParticipantStatus.PENDING || currentStatus == ScheduleParticipantStatus.REJECTED) 
               && status == ScheduleParticipantStatus.ACCEPTED) {
      schedule.addCurrentUserCount();
      participant.accept();
    }
  }
}
