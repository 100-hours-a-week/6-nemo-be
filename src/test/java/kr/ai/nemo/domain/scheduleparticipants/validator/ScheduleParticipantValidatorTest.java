package kr.ai.nemo.domain.scheduleparticipants.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import kr.ai.nemo.domain.scheduleparticipants.domain.enums.ScheduleParticipantStatus;
import kr.ai.nemo.domain.scheduleparticipants.exception.ScheduleParticipantErrorCode;
import kr.ai.nemo.domain.scheduleparticipants.exception.ScheduleParticipantException;
import kr.ai.nemo.domain.scheduleparticipants.repository.ScheduleParticipantRepository;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.unit.global.fixture.schedule.ScheduleFixture;
import kr.ai.nemo.unit.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleParticipantValidator 테스트")
class ScheduleParticipantValidatorTest {

    @Mock
    private ScheduleParticipantRepository scheduleParticipantRepository;

    @InjectMocks
    private ScheduleParticipantValidator scheduleParticipantValidator;

    @Test
    @DisplayName("[성공] 스케줄 참여 검증")
    void validateParticipationOrThrow_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        Schedule schedule = ScheduleFixture.createDefaultSchedule(user);
        ScheduleParticipant participant = ScheduleParticipant.builder()
            .id(1L) // 테스트 시 수동 설정 가능 (실제 저장 시에는 DB가 자동 생성)
            .schedule(schedule) // Schedule 객체 필요
            .user(user)         // User 객체 필요
            .status(ScheduleParticipantStatus.PENDING) // 예: 초기 상태
            .joinedAt(null)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        given(scheduleParticipantRepository.findByScheduleIdAndUserId(schedule.getId(), user.getId()))
                .willReturn(Optional.of(participant));

        // when
        ScheduleParticipant result = scheduleParticipantValidator.validateParticipationOrThrow(schedule.getId(), user.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(participant);
    }

    @Test
    @DisplayName("[실패] 스케줄 참여 검증 - 참여하지 않은 사용자")
    void validateParticipationOrThrow_NotParticipant_ThrowException() {
        // given
        Long scheduleId = 1L;
        Long userId = 100L;

        given(scheduleParticipantRepository.findByScheduleIdAndUserId(scheduleId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleParticipantValidator.validateParticipationOrThrow(scheduleId, userId))
                .isInstanceOf(ScheduleParticipantException.class)
                .hasFieldOrPropertyWithValue("errorCode", ScheduleParticipantErrorCode.NOT_GROUP_MEMBER);
    }

    @Test
    @DisplayName("[성공] 상태 변경 검증 - 다른 상태로 변경")
    void validateStatusChange_DifferentStatus_Success() {
        // given
        ScheduleParticipantStatus currentStatus = ScheduleParticipantStatus.PENDING;
        ScheduleParticipantStatus newStatus = ScheduleParticipantStatus.ACCEPTED;

        // when & then (예외가 발생하지 않아야 함)
        scheduleParticipantValidator.validateStatusChange(currentStatus, newStatus);
    }

    @Test
    @DisplayName("[실패] 상태 변경 검증 - 동일한 상태로 변경")
    void validateStatusChange_SameStatus_ThrowException() {
        // given
        ScheduleParticipantStatus currentStatus = ScheduleParticipantStatus.ACCEPTED;
        ScheduleParticipantStatus newStatus = ScheduleParticipantStatus.ACCEPTED;

        // when & then
        assertThatThrownBy(() -> scheduleParticipantValidator.validateStatusChange(currentStatus, newStatus))
                .isInstanceOf(ScheduleParticipantException.class)
                .hasFieldOrPropertyWithValue("errorCode", ScheduleParticipantErrorCode.SCHEDULE_ALREADY_DECIDED);
    }

    @Test
    @DisplayName("[성공] 상태 변경 검증 - PENDING에서 REJECTED로 변경")
    void validateStatusChange_PendingToDeclined_Success() {
        // given
        ScheduleParticipantStatus currentStatus = ScheduleParticipantStatus.PENDING;
        ScheduleParticipantStatus newStatus = ScheduleParticipantStatus.REJECTED;

        // when & then (예외가 발생하지 않아야 함)
        scheduleParticipantValidator.validateStatusChange(currentStatus, newStatus);
    }

    @Test
    @DisplayName("[실패] 상태 변경 검증 - DECLINED에서 REJECTED로 변경")
    void validateStatusChange_DeclinedToDeclined_ThrowException() {
        // given
        ScheduleParticipantStatus currentStatus = ScheduleParticipantStatus.REJECTED;
        ScheduleParticipantStatus newStatus = ScheduleParticipantStatus.REJECTED;

        // when & then
        assertThatThrownBy(() -> scheduleParticipantValidator.validateStatusChange(currentStatus, newStatus))
                .isInstanceOf(ScheduleParticipantException.class)
                .hasFieldOrPropertyWithValue("errorCode", ScheduleParticipantErrorCode.SCHEDULE_ALREADY_DECIDED);
    }
}
