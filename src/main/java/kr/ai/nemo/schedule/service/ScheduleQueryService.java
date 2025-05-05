package kr.ai.nemo.schedule.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.dto.ScheduleDetailResponse;
import kr.ai.nemo.schedule.dto.ScheduleListResponse;
import kr.ai.nemo.schedule.participants.domain.ScheduleParticipant;
import kr.ai.nemo.schedule.participants.repository.ScheduleParticipantRepository;
import kr.ai.nemo.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleQueryService {
  private final ScheduleCommandService scheduleCommandService;
  private final ScheduleRepository scheduleRepository;
  private final ScheduleParticipantRepository scheduleParticipantRepository;

  public ScheduleDetailResponse getScheduleDetail(Long scheduleId) {
    Schedule schedule = scheduleCommandService.findByIdOrThrow(scheduleId);
    List<ScheduleParticipant> participants = scheduleParticipantRepository.findByScheduleId(scheduleId);
    return ScheduleDetailResponse.from(schedule, participants);
  }

  public ScheduleListResponse getGroupSchedules(Long groupId, PageRequest pageRequest) {
    Page<Schedule> page = scheduleRepository.findByGroupId(groupId, pageRequest);

    List<ScheduleListResponse.ScheduleSummary> summaries = page.getContent().stream()
        .map(schedule -> new ScheduleListResponse.ScheduleSummary(
            schedule.getId(),
            schedule.getTitle(),
            schedule.getStartAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            schedule.getAddress(),
            schedule.getDescription(),
            schedule.getOwner().getNickname(),
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

}
