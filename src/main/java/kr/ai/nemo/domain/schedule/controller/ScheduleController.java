package kr.ai.nemo.domain.schedule.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ai.nemo.aop.role.annotation.RequireGroupParticipant;
import kr.ai.nemo.aop.role.annotation.RequireScheduleOwner;
import kr.ai.nemo.global.common.BaseApiResponse;
import kr.ai.nemo.domain.schedule.dto.response.MySchedulesResponse;
import kr.ai.nemo.domain.schedule.dto.request.ScheduleCreateRequest;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleCreateResponse;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleDetailResponse;
import kr.ai.nemo.domain.schedule.service.ScheduleCommandService;
import kr.ai.nemo.domain.schedule.service.ScheduleQueryService;
import kr.ai.nemo.global.swagger.schedule.SwaggerMySchedulesResponse;
import kr.ai.nemo.global.swagger.schedule.SwaggerScheduleCreateResponse;
import kr.ai.nemo.global.swagger.schedule.SwaggerScheduleDetailResponse;
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

@Tag(name = "일정 API", description = "일정 관련 API 입니다.")
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {
  private final ScheduleCommandService scheduleCommandService;
  private final ScheduleQueryService scheduleQueryService;

  @Operation(summary = "일정 생성", description = "일정을 생성합니다.")
  @ApiResponse(responseCode = "201", description = "리소스가 성공적으로 생성되었습니다.", content = @Content(schema = @Schema(implementation = SwaggerScheduleCreateResponse.class)))
  @ApiResponse(responseCode = "404", description = "모임원이 아닙니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @PostMapping
  @RequireGroupParticipant
  public ResponseEntity<BaseApiResponse<ScheduleCreateResponse>> createSchedule(
      @RequestBody ScheduleCreateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseApiResponse.created(scheduleCommandService.createSchedule(userId, request)));
  }

  @Operation(summary = "일정 취소(삭제)", description = "일정을 취소(삭제)합니다.")
  @ApiResponse(responseCode = "204", description = "성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @ApiResponse(responseCode = "403", description = "일정 생성자만 취소할 수 있습니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @ApiResponse(responseCode = "409", description = "이미 종료된 일정을 취소할 수 없습니다. // 이미 취소된 일정입니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @RequireScheduleOwner("scheduleId")
  @DeleteMapping("/{scheduleId}")
  public ResponseEntity<BaseApiResponse<Void>> deleteSchedule(
      @Parameter(description = "조회할 일정 ID", example = "123", required = true)
      @PathVariable Long scheduleId) {
    scheduleCommandService.deleteSchedule(scheduleId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "일정 상세 조회", description = "일정 상세를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = SwaggerScheduleDetailResponse.class)))
  @GetMapping("/{scheduleId}")
  public ResponseEntity<BaseApiResponse<ScheduleDetailResponse>> getScheduleDetail(@PathVariable Long scheduleId) {
    return ResponseEntity.ok(BaseApiResponse.success(scheduleQueryService.getScheduleDetail(scheduleId)));
  }

  @Operation(summary = "나의 일정 리스트 조회", description = "나의 일정 리스트를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "요청이 성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = SwaggerMySchedulesResponse.class)))
  @GetMapping("/me")
  public ResponseEntity<BaseApiResponse<MySchedulesResponse>> getMySchedules(
      @Parameter(hidden = true) @AuthenticationPrincipal Long userId
  ) {
    return ResponseEntity.ok(BaseApiResponse.success(scheduleQueryService.getMySchedules(userId)));
  }
}
