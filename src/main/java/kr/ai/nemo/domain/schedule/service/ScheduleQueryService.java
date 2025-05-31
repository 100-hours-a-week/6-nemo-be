package kr.ai.nemo.domain.schedule.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.dto.response.MySchedulesResponse;
import kr.ai.nemo.domain.schedule.dto.response.MySchedulesResponse.ScheduleParticipation;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleDetailResponse;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleListResponse;
import kr.ai.nemo.domain.schedule.validator.ScheduleValidator;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import kr.ai.nemo.domain.scheduleparticipants.domain.enums.ScheduleParticipantStatus;
import kr.ai.nemo.domain.scheduleparticipants.repository.ScheduleParticipantRepository;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleQueryService {
  private final ScheduleRepository scheduleRepository;
  private final ScheduleParticipantRepository scheduleParticipantRepository;
  private final GroupValidator groupValidator;
  private final ScheduleValidator scheduleValidator;

  @TimeTrace
  public ScheduleDetailResponse getScheduleDetail(Long scheduleId) {
    Schedule schedule = scheduleValidator.findByIdOrThrow(scheduleId);

    List<ScheduleParticipant> participants = scheduleParticipantRepository.findByScheduleId(scheduleId);
    return ScheduleDetailResponse.from(schedule, participants);
  }

  @TimeTrace
  public ScheduleListResponse getGroupSchedules(Long groupId, PageRequest pageRequest) {
    groupValidator.findByIdOrThrow(groupId);
    Page<Schedule> page = scheduleRepository.findByGroupIdAndStatusNot(groupId, pageRequest, ScheduleStatus.CANCELED);

    List<ScheduleListResponse.ScheduleSummary> summaries = page.getContent().stream()
        .map(schedule -> new ScheduleListResponse.ScheduleSummary(
            schedule.getId(),
            schedule.getTitle(),
            schedule.getDescription(),
            schedule.getAddress(),
            schedule.getStatus(),
            schedule.getCurrentUserCount(),
            schedule.getOwner().getNickname(),
            schedule.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            schedule.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        ))
        .toList();

    return new ScheduleListResponse(
        summaries,
        page.getTotalPages(),
        page.getTotalElements(),
        page.getNumber(),
        page.isLast()
    );
  }

  @TimeTrace
  public MySchedulesResponse getMySchedules(Long userId) {
    List<ScheduleParticipant> participants = scheduleParticipantRepository.findByUserId(userId);

    LocalDateTime now = LocalDateTime.now();

    List<ScheduleParticipation> pending = participants.stream()
        .filter(p -> p.getStatus() == ScheduleParticipantStatus.PENDING)
        .map(ScheduleParticipation::from)
        .toList();

    List<ScheduleParticipation> upcoming = participants.stream()
        .filter(p -> p.getStatus() == ScheduleParticipantStatus.ACCEPTED
            && p.getSchedule().getStartAt().isAfter(now))
        .map(ScheduleParticipation::from)
        .toList();

    List<ScheduleParticipation> completed = participants.stream()
        .filter(p -> p.getStatus() == ScheduleParticipantStatus.ACCEPTED
            && p.getSchedule().getStartAt().isBefore(now))
        .map(ScheduleParticipation::from)
        .toList();

    return new MySchedulesResponse(pending, upcoming, completed);
    }
}
