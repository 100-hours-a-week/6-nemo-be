package kr.ai.nemo.domain.scheduleparticipants.dto;

import jakarta.validation.constraints.NotNull;
import kr.ai.nemo.domain.scheduleparticipants.domain.enums.ScheduleParticipantStatus;

public record ScheduleParticipantDecisionRequest(
    @NotNull ScheduleParticipantStatus status
) {}
