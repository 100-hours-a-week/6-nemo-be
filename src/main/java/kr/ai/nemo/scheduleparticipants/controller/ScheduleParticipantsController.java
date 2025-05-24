package kr.ai.nemo.scheduleparticipants.controller;

import jakarta.validation.Valid;
import kr.ai.nemo.global.common.ApiResponse;
import kr.ai.nemo.scheduleparticipants.dto.ScheduleParticipantDecisionRequest;
import kr.ai.nemo.scheduleparticipants.service.ScheduleParticipantsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schedules")
public class ScheduleParticipantsController {
  private final ScheduleParticipantsService scheduleParticipantsService;

  @PatchMapping("/{scheduleId}/participants")
  public ResponseEntity<ApiResponse<Void>> updateParticipants(@PathVariable Long scheduleId, @Valid @RequestBody ScheduleParticipantDecisionRequest request, @AuthenticationPrincipal Long userId) {
    scheduleParticipantsService.decideParticipation(scheduleId, userId, request.status());
    return ResponseEntity.ok(ApiResponse.noContent());
  }
}
