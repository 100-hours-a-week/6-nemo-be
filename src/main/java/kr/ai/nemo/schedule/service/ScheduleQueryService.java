package kr.ai.nemo.schedule.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.ai.nemo.group.validator.GroupValidator;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.schedule.dto.MySchedulesResponse;
import kr.ai.nemo.schedule.dto.MySchedulesResponse.ScheduleParticipation;
import kr.ai.nemo.schedule.dto.ScheduleDetailResponse;
import kr.ai.nemo.schedule.dto.ScheduleListResponse;
import kr.ai.nemo.schedule.validator.ScheduleValidator;
import kr.ai.nemo.scheduleparticipants.domain.ScheduleParticipant;
import kr.ai.nemo.scheduleparticipants.domain.enums.ScheduleParticipantStatus;
import kr.ai.nemo.scheduleparticipants.repository.ScheduleParticipantRepository;
import kr.ai.nemo.schedule.repository.ScheduleRepository;
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


  public ScheduleDetailResponse getScheduleDetail(Long scheduleId) {
    Schedule schedule = scheduleValidator.findByIdOrThrow(scheduleId);

    List<ScheduleParticipant> participants = scheduleParticipantRepository.findByScheduleId(scheduleId);
    return ScheduleDetailResponse.from(schedule, participants);
  }

  public ScheduleListResponse getGroupSchedules(Long groupId, PageRequest pageRequest) {
    groupValidator.findByIdOrThrow(groupId);
    Page<Schedule> page = scheduleRepository.findByGroupIdAndStatusNot(groupId, pageRequest, ScheduleStatus.CANCELED);

    List<ScheduleListResponse.ScheduleSummary> summaries = page.getContent().stream()
        .map(schedule -> new ScheduleListResponse.ScheduleSummary(
            schedule.getId(),
            schedule.getTitle(),
            schedule.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            schedule.getAddress(),
            schedule.getDescription(),
            schedule.getOwner().getNickname(),
            schedule.getStatus().name(),
            schedule.getCurrentUserCount()
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
