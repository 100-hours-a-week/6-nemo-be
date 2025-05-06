package kr.ai.nemo.schedule.controller;

import java.net.URI;
import kr.ai.nemo.common.UriGenerator;
import kr.ai.nemo.common.exception.ApiResponse;
import kr.ai.nemo.schedule.dto.MySchedulesResponse;
import kr.ai.nemo.schedule.dto.ScheduleCreateRequest;
import kr.ai.nemo.schedule.dto.ScheduleCreateResponse;
import kr.ai.nemo.schedule.dto.ScheduleDetailResponse;
import kr.ai.nemo.schedule.service.ScheduleCommandService;
import kr.ai.nemo.schedule.service.ScheduleQueryService;
import lombok.RequiredArgsConstructor;
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
    ScheduleCreateResponse response = scheduleCommandService.createSchedule(userId, request);
    URI location = UriGenerator.scheduleDetail(response.group().groupId());
    return ResponseEntity.created(location).body(ApiResponse.created(response));
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
    ScheduleDetailResponse response = scheduleQueryService.getScheduleDetail(scheduleId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<MySchedulesResponse>> getMySchedules(
      @AuthenticationPrincipal Long userId
  ) {
    return ResponseEntity.ok(ApiResponse.success(scheduleQueryService.getMySchedules(userId)));
  }
}
