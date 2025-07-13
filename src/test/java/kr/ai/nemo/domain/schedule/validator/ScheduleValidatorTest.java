package kr.ai.nemo.domain.schedule.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.exception.ScheduleErrorCode;
import kr.ai.nemo.domain.schedule.exception.ScheduleException;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.unit.global.fixture.schedule.ScheduleFixture;
import kr.ai.nemo.unit.global.fixture.user.UserFixture;
import kr.ai.nemo.unit.global.testUtil.TestReflectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleValidator 테스트")
class ScheduleValidatorTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleValidator scheduleValidator;

    @Test
    @DisplayName("[성공] 스케줄 ID로 조회")
    void findByIdOrThrow_Success() {
        // given
        Long scheduleId = 1L;
        User owner = UserFixture.createDefaultUser();
        Schedule schedule = ScheduleFixture.createDefaultSchedule(owner);

        given(scheduleRepository.findByIdWithGroupAndOwner(scheduleId))
                .willReturn(Optional.of(schedule));

        // when
        Schedule result = scheduleValidator.findByIdOrThrow(scheduleId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(schedule);
    }

    @Test
    @DisplayName("[실패] 스케줄 ID로 조회 - 스케줄 없음")
    void findByIdOrThrow_ScheduleNotFound_ThrowException() {
        // given
        Long scheduleId = 1L;

        given(scheduleRepository.findByIdWithGroupAndOwner(scheduleId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> scheduleValidator.findByIdOrThrow(scheduleId))
                .isInstanceOf(ScheduleException.class)
                .hasFieldOrPropertyWithValue("errorCode", ScheduleErrorCode.SCHEDULE_NOT_FOUND);
    }

    @Test
    @DisplayName("[실패] 스케줄 ID로 조회 - 취소된 스케줄")
    void findByIdOrThrow_ScheduleCanceled_ThrowException() {
        // given
        Long scheduleId = 1L;
        User owner = UserFixture.createDefaultUser();
        Schedule schedule = ScheduleFixture.createScheduleWithStatus(owner, ScheduleStatus.CANCELED);

        given(scheduleRepository.findByIdWithGroupAndOwner(scheduleId))
                .willReturn(Optional.of(schedule));

        // when & then
        assertThatThrownBy(() -> scheduleValidator.findByIdOrThrow(scheduleId))
                .isInstanceOf(ScheduleException.class)
                .hasFieldOrPropertyWithValue("errorCode", ScheduleErrorCode.SCHEDULE_ALREADY_CANCELED);
    }

    @Test
    @DisplayName("[성공] 스케줄 검증 - 활성 상태")
    void validateSchedule_ActiveSchedule_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Schedule schedule = ScheduleFixture.createScheduleWithStatus(owner, ScheduleStatus.RECRUITING);

        // when & then (예외가 발생하지 않아야 함)
        scheduleValidator.validateSchedule(schedule);
    }

    @Test
    @DisplayName("[실패] 스케줄 검증 - 종료된 스케줄")
    void validateSchedule_ClosedSchedule_ThrowException() {
        // given
        User owner = UserFixture.createDefaultUser();
        Schedule schedule = ScheduleFixture.createScheduleWithStatus(owner, ScheduleStatus.CLOSED);

        // when & then
        assertThatThrownBy(() -> scheduleValidator.validateSchedule(schedule))
                .isInstanceOf(ScheduleException.class)
                .hasFieldOrPropertyWithValue("errorCode", ScheduleErrorCode.SCHEDULE_ALREADY_ENDED);
    }

    @Test
    @DisplayName("[실패] 스케줄 검증 - 취소된 스케줄")
    void validateSchedule_CanceledSchedule_ThrowException() {
        // given
        User owner = UserFixture.createDefaultUser();
        Schedule schedule = ScheduleFixture.createScheduleWithStatus(owner, ScheduleStatus.CANCELED);

        // when & then
        assertThatThrownBy(() -> scheduleValidator.validateSchedule(schedule))
                .isInstanceOf(ScheduleException.class)
                .hasFieldOrPropertyWithValue("errorCode", ScheduleErrorCode.SCHEDULE_ALREADY_CANCELED);
    }

    @Test
    @DisplayName("[실패] 스케줄 소유자 검증 - 소유자가 동일한 경우")
    void validateScheduleOwner_SameOwner_ThrowException() {
        // given
        User owner = UserFixture.createDefaultUser();
        TestReflectionUtils.setField(owner, "id", 1L);
        Schedule schedule = ScheduleFixture.createDefaultSchedule(owner);
        Long userId = owner.getId();

        // when & then
        assertThatThrownBy(() -> scheduleValidator.validateScheduleOwner(userId, schedule))
                .isInstanceOf(ScheduleException.class)
                .hasFieldOrPropertyWithValue("errorCode", ScheduleErrorCode.SCHEDULE_DELETE_FORBIDDEN);
    }

    @Test
    @DisplayName("[성공] 스케줄 소유자 검증 - 소유자가 다른 경우")
    void validateScheduleOwner_DifferentOwner_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Schedule schedule = ScheduleFixture.createDefaultSchedule(owner);
        Long differentUserId = 999L;

        // when & then (예외가 발생하지 않아야 함)
        scheduleValidator.validateScheduleOwner(differentUserId, schedule);
    }

    @Test
    @DisplayName("[성공] 스케줄 시작 검증 - 열린 상태")
    void validateScheduleStart_OpenSchedule_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Schedule schedule = ScheduleFixture.createScheduleWithStatus(owner, ScheduleStatus.RECRUITING);

        // when & then (예외가 발생하지 않아야 함)
        scheduleValidator.validateScheduleStart(schedule);
    }

    @Test
    @DisplayName("[실패] 스케줄 시작 검증 - 종료된 스케줄")
    void validateScheduleStart_ClosedSchedule_ThrowException() {
        // given
        User owner = UserFixture.createDefaultUser();
        Schedule schedule = ScheduleFixture.createScheduleWithStatus(owner, ScheduleStatus.CLOSED);

        // when & then
        assertThatThrownBy(() -> scheduleValidator.validateScheduleStart(schedule))
                .isInstanceOf(ScheduleException.class)
                .hasFieldOrPropertyWithValue("errorCode", ScheduleErrorCode.SCHEDULE_ALREADY_STARTED_OR_ENDED);
    }
}
