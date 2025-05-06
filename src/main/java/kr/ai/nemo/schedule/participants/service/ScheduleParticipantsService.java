package kr.ai.nemo.schedule.participants.service;

import java.time.LocalDateTime;
import java.util.List;
import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.participants.domain.GroupParticipants;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.schedule.participants.domain.ScheduleParticipant;
import kr.ai.nemo.schedule.participants.domain.enums.ScheduleParticipantStatus;
import kr.ai.nemo.schedule.participants.repository.ScheduleParticipantRepository;
import kr.ai.nemo.schedule.repository.ScheduleRepository;
import kr.ai.nemo.schedule.service.ScheduleQueryService;
import kr.ai.nemo.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleParticipantsService {
  private final ScheduleParticipantRepository scheduleParticipantRepository;
  private final ScheduleRepository scheduleRepository;
  private final ScheduleQueryService scheduleQueryService;

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

  @Transactional
  public void decideParticipation(Long scheduleId, Long userId, ScheduleParticipantStatus status) {


    ScheduleParticipant participant = scheduleParticipantRepository
        .findByScheduleIdAndUserId(scheduleId, userId)
        .orElseThrow(() -> new CustomException(ResponseCode.ACCESS_DENIED));

    if (participant.getStatus() != ScheduleParticipantStatus.PENDING) {
      throw new CustomException(ResponseCode.SCHEDULE_ALREADY_DECIDED);
    }
    if (status == ScheduleParticipantStatus.ACCEPTED) {
      scheduleQueryService.findByIdOrThrow(scheduleId);
    }

    participant.setStatus(status);
    participant.setJoinedAt(LocalDateTime.now());
  }
}
