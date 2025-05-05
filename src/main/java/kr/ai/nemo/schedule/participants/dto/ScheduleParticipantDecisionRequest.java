package kr.ai.nemo.schedule.participants.dto;

import jakarta.validation.constraints.NotNull;
import kr.ai.nemo.schedule.participants.domain.enums.ScheduleParticipantStatus;

public record ScheduleParticipantDecisionRequest(
    @NotNull ScheduleParticipantStatus status
) {}
