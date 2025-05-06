package kr.ai.nemo.schedule.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import kr.ai.nemo.group.service.GroupQueryService;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.schedule.dto.MySchedulesResponse;
import kr.ai.nemo.schedule.dto.MySchedulesResponse.ScheduleParticipation;
import kr.ai.nemo.schedule.dto.ScheduleDetailResponse;
import kr.ai.nemo.schedule.dto.ScheduleListResponse;
import kr.ai.nemo.schedule.participants.domain.ScheduleParticipant;
import kr.ai.nemo.schedule.participants.domain.enums.ScheduleParticipantStatus;
import kr.ai.nemo.schedule.participants.repository.ScheduleParticipantRepository;
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
  private final GroupQueryService groupQueryService;

  public ScheduleDetailResponse getScheduleDetail(Long scheduleId) {
    Schedule schedule = findByIdOrThrow(scheduleId);
    List<ScheduleParticipant> participants = scheduleParticipantRepository.findByScheduleId(scheduleId);
    return ScheduleDetailResponse.from(schedule, participants);
  }

  public ScheduleListResponse getGroupSchedules(Long groupId, PageRequest pageRequest) {
    groupQueryService.findByIdOrThrow(groupId);
    Page<Schedule> page = scheduleRepository.findByGroupId(groupId, pageRequest);

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

    public Schedule findByIdOrThrow(Long scheduleId) {
      Schedule schedule = scheduleRepository.findById(scheduleId)
          .orElseThrow(() -> new CustomException(ResponseCode.SCHEDULE_NOT_FOUND));
      if(schedule.getStatus() == ScheduleStatus.CANCELED){
        throw new CustomException(ResponseCode.SCHEDULE_ALREADY_CANCELED);
      }
      return schedule;
    }
}
