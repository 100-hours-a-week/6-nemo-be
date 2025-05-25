package kr.ai.nemo.schedule.controller;

import kr.ai.nemo.global.common.ApiResponse;
import kr.ai.nemo.schedule.dto.response.MySchedulesResponse;
import kr.ai.nemo.schedule.dto.request.ScheduleCreateRequest;
import kr.ai.nemo.schedule.dto.response.ScheduleCreateResponse;
import kr.ai.nemo.schedule.dto.response.ScheduleDetailResponse;
import kr.ai.nemo.schedule.service.ScheduleCommandService;
import kr.ai.nemo.schedule.service.ScheduleQueryService;
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
  public ResponseEntity<ApiResponse<ScheduleCreateResponse>> createSchedule(
      @RequestBody ScheduleCreateRequest request,
      @AuthenticationPrincipal Long userId) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(scheduleCommandService.createSchedule(userId, request)));
  }

  @DeleteMapping("/{scheduleId}")
  public ResponseEntity<ApiResponse<Void>> deleteSchedule(
      @PathVariable Long scheduleId,
      @AuthenticationPrincipal Long userId) {
    scheduleCommandService.deleteSchedule(userId, scheduleId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{scheduleId}")
  public ResponseEntity<ApiResponse<ScheduleDetailResponse>> getScheduleDetail(@PathVariable Long scheduleId) {
    return ResponseEntity.ok(ApiResponse.success(scheduleQueryService.getScheduleDetail(scheduleId)));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<MySchedulesResponse>> getMySchedules(
      @AuthenticationPrincipal Long userId
  ) {
    return ResponseEntity.ok(ApiResponse.success(scheduleQueryService.getMySchedules(userId)));
  }
}
