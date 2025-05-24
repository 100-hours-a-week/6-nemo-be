package kr.ai.nemo.scheduleparticipants.dto;

import jakarta.validation.constraints.NotNull;
import kr.ai.nemo.scheduleparticipants.domain.enums.ScheduleParticipantStatus;

public record ScheduleParticipantDecisionRequest(
    @NotNull ScheduleParticipantStatus status
) {}
