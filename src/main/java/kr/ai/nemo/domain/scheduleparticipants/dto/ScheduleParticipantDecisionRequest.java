package kr.ai.nemo.domain.scheduleparticipants.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.ai.nemo.domain.scheduleparticipants.domain.enums.ScheduleParticipantStatus;

@Schema(name = "일정 참/불참 선택 요청", description = "일정 참/불참 선택 요청 DTO")
public record ScheduleParticipantDecisionRequest(

    @Schema(description = "참/불참", example = "ACCEPTED")
    @NotNull
    ScheduleParticipantStatus status
) {}
