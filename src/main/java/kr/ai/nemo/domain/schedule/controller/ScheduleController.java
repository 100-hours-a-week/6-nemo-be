package kr.ai.nemo.domain.schedule.controller;

import kr.ai.nemo.global.common.BaseApiResponse;
import kr.ai.nemo.domain.schedule.dto.response.MySchedulesResponse;
import kr.ai.nemo.domain.schedule.dto.request.ScheduleCreateRequest;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleCreateResponse;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleDetailResponse;
import kr.ai.nemo.domain.schedule.service.ScheduleCommandService;
import kr.ai.nemo.domain.schedule.service.ScheduleQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {
  private final ScheduleCommandService scheduleCommandService;
  private final ScheduleQueryService scheduleQueryService;

  @PostMapping
  public ResponseEntity<BaseApiResponse<ScheduleCreateResponse>> createSchedule(
      @RequestBody ScheduleCreateRequest request,
      @AuthenticationPrincipal Long userId) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseApiResponse.created(scheduleCommandService.createSchedule(userId, request)));
  }

  @DeleteMapping("/{scheduleId}")
  public ResponseEntity<BaseApiResponse<Void>> deleteSchedule(
      @PathVariable Long scheduleId,
      @AuthenticationPrincipal Long userId) {
    scheduleCommandService.deleteSchedule(userId, scheduleId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{scheduleId}")
  public ResponseEntity<BaseApiResponse<ScheduleDetailResponse>> getScheduleDetail(@PathVariable Long scheduleId) {
    return ResponseEntity.ok(BaseApiResponse.success(scheduleQueryService.getScheduleDetail(scheduleId)));
  }

  @GetMapping("/me")
  public ResponseEntity<BaseApiResponse<MySchedulesResponse>> getMySchedules(
      @AuthenticationPrincipal Long userId
  ) {
    return ResponseEntity.ok(BaseApiResponse.success(scheduleQueryService.getMySchedules(userId)));
  }
}
