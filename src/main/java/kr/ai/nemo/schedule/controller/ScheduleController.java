package kr.ai.nemo.schedule.controller;

import java.net.URI;
import kr.ai.nemo.common.UriGenerator;
import kr.ai.nemo.common.exception.ApiResponse;
import kr.ai.nemo.schedule.domain.Schedule;
import kr.ai.nemo.schedule.service.ScheduleCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {
  private final ScheduleCommandService scheduleCommandService;
  UriGenerator uriGenerator;

  @PostMapping
  public ResponseEntity<ApiResponse<SchduleResponse>> schedule() {
    Schedule schedule = scheduleCommandService.createSchedule();
    URI location = uriGenerator.getUri(schedule.getId());
    return ResponseEntity.created(location).body(null);
  }
}
