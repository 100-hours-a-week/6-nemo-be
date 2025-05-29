package kr.ai.nemo.domain.scheduleparticipants.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.global.common.BaseApiResponse;
import kr.ai.nemo.domain.scheduleparticipants.dto.ScheduleParticipantDecisionRequest;
import kr.ai.nemo.domain.scheduleparticipants.service.ScheduleParticipantsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "일정 참여자 API", description = "일정 참여자 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schedules")
public class ScheduleParticipantsController {
  private final ScheduleParticipantsService scheduleParticipantsService;

  @Operation(summary = "일정 참/불참 선택", description = "사용자가 일정을 참여할지 불참여할지 선택합니다.")
  @ApiResponse(responseCode = "204", description = "성공적으로 처리되었습니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @ApiResponse(responseCode = "403", description = "모임원이 아니므로 일정에 참여할 수 없습니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @ApiResponse(responseCode = "409", description = "이미 응답하셨습니다. // 이미 종료된 일정입니다. // 이미 취소된 일정입니다.", content = @Content(schema = @Schema(implementation = BaseApiResponse.class)))
  @PatchMapping("/{scheduleId}/participants")
  public ResponseEntity<BaseApiResponse<Void>> updateParticipants(
      @PathVariable Long scheduleId,
      @Valid @RequestBody ScheduleParticipantDecisionRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
    scheduleParticipantsService.decideParticipation(scheduleId, userDetails.getUserId(), request.status());
    return ResponseEntity.ok(BaseApiResponse.noContent());
  }
}
