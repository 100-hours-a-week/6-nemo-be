package kr.ai.nemo.unit.global.fixture.schedule;

import java.time.LocalDateTime;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.unit.global.testUtil.TestReflectionUtils;

/**
 * 테스트용 Schedule 픽스처 클래스
 */
public class ScheduleFixture {

    public static Schedule createSchedule(User owner, Group group, String title, LocalDateTime startAt) {
        return Schedule.builder()
                .group(group)
                .owner(owner)
                .title(title)
                .description("테스트 스케줄 설명")
                .address("서울시 강남구 테헤란로 123")
                .currentUserCount(1)
                .status(ScheduleStatus.RECRUITING)
                .startAt(startAt)
                .build();
    }

    public static Schedule createDefaultSchedule(User owner, Group group) {
        return createSchedule(owner, group, "테스트 스케줄", LocalDateTime.now().plusDays(1));
    }

    public static Schedule createCanceledSchedule(User owner, Group group) {
        Schedule schedule = createSchedule(owner, group, "취소된 스케줄", LocalDateTime.now().plusDays(1));
        schedule.cancel();
        return schedule;
    }

    public static Schedule createClosedSchedule(User owner, Group group) {
        Schedule schedule = createSchedule(owner, group, "종료된 스케줄", LocalDateTime.now().minusDays(1));
        schedule.complete();
        return schedule;
    }

    public static Schedule createScheduleWithId(Long id, User owner, Group group, String title) {
        Schedule schedule = createSchedule(owner, group, title, LocalDateTime.now().plusDays(1));
        TestReflectionUtils.setField(schedule, "id", id);
        return schedule;
    }

    public static Schedule createDefaultSchedule(User owner) {
        return Schedule.builder()
                .owner(owner)
                .title("테스트 스케줄")
                .description("테스트 스케줄 설명")
                .address("서울시 강남구 테헤란로 123")
                .currentUserCount(1)
                .status(ScheduleStatus.RECRUITING)
                .startAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    public static Schedule createScheduleWithStatus(User owner, ScheduleStatus status) {
        return Schedule.builder()
                .owner(owner)
                .title("테스트 스케줄")
                .description("테스트 스케줄 설명")
                .address("서울시 강남구 테헤란로 123")
                .currentUserCount(1)
                .status(status)
                .startAt(LocalDateTime.now().plusDays(1))
                .build();
    }
}
