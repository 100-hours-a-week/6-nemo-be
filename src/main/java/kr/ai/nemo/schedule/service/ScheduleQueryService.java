package kr.ai.nemo.schedule.service;

import java.util.List;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.dto.ScheduleDetailResponse;
import kr.ai.nemo.schedule.participants.domain.ScheduleParticipant;
import kr.ai.nemo.schedule.participants.repository.ScheduleParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScheduleQueryService {
  private final ScheduleCommandService scheduleCommandService;
  private final ScheduleParticipantRepository scheduleParticipantRepository;

  public ScheduleDetailResponse getScheduleDetail(Long scheduleId) {
    Schedule schedule = scheduleCommandService.findByIdOrThrow(scheduleId);
    List<ScheduleParticipant> participants = scheduleParticipantRepository.findByScheduleId(scheduleId);
    return ScheduleDetailResponse.from(schedule, participants);
  }
}
