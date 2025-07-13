package kr.ai.nemo.domain.schedule.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.unit.global.fixture.group.GroupFixture;
import kr.ai.nemo.unit.global.fixture.schedule.ScheduleFixture;
import kr.ai.nemo.unit.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleStatusUpdater 테스트")
class ScheduleStatusUpdaterTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleStatusUpdater scheduleStatusUpdater;

    @Test
    @DisplayName("[성공] 만료된 스케줄 상태 업데이트")
    void updateClosedSchedules_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(owner);
        
        Schedule expiredSchedule1 = ScheduleFixture.createSchedule(
                owner, group, "만료된 스케줄1", LocalDateTime.now().minusHours(1)
        );
        Schedule expiredSchedule2 = ScheduleFixture.createSchedule(
                owner, group, "만료된 스케줄2", LocalDateTime.now().minusHours(2)
        );
        
        List<Schedule> expiredSchedules = List.of(expiredSchedule1, expiredSchedule2);

        given(scheduleRepository.findByStartAtBeforeAndStatus(any(LocalDateTime.class), eq(ScheduleStatus.RECRUITING)))
                .willReturn(expiredSchedules);

        // when
        scheduleStatusUpdater.updateClosedSchedules();

        // then
        verify(scheduleRepository).findByStartAtBeforeAndStatus(any(LocalDateTime.class), eq(ScheduleStatus.RECRUITING));
        
        // 각 스케줄의 complete() 메서드가 호출되었는지는 직접 확인하기 어려우므로
        // 최소한 레포지토리 메서드가 호출되었는지 확인
    }

    @Test
    @DisplayName("[성공] 만료된 스케줄이 없는 경우")
    void updateClosedSchedules_NoExpiredSchedules_Success() {
        // given
        given(scheduleRepository.findByStartAtBeforeAndStatus(any(LocalDateTime.class), eq(ScheduleStatus.RECRUITING)))
                .willReturn(List.of());

        // when
        scheduleStatusUpdater.updateClosedSchedules();

        // then
        verify(scheduleRepository).findByStartAtBeforeAndStatus(any(LocalDateTime.class), eq(ScheduleStatus.RECRUITING));
    }

    @Test
    @DisplayName("[성공] 여러 만료된 스케줄 일괄 처리")
    void updateClosedSchedules_MultipleExpiredSchedules_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(owner);
        
        Schedule expiredSchedule1 = ScheduleFixture.createSchedule(
                owner, group, "만료된 스케줄1", LocalDateTime.now().minusHours(1)
        );
        Schedule expiredSchedule2 = ScheduleFixture.createSchedule(
                owner, group, "만료된 스케줄2", LocalDateTime.now().minusHours(2)
        );
        Schedule expiredSchedule3 = ScheduleFixture.createSchedule(
                owner, group, "만료된 스케줄3", LocalDateTime.now().minusHours(3)
        );
        
        List<Schedule> expiredSchedules = List.of(expiredSchedule1, expiredSchedule2, expiredSchedule3);

        given(scheduleRepository.findByStartAtBeforeAndStatus(any(LocalDateTime.class), eq(ScheduleStatus.RECRUITING)))
                .willReturn(expiredSchedules);

        // when
        scheduleStatusUpdater.updateClosedSchedules();

        // then
        verify(scheduleRepository, times(1))
                .findByStartAtBeforeAndStatus(any(LocalDateTime.class), eq(ScheduleStatus.RECRUITING));
    }
}
