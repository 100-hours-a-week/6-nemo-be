package kr.ai.nemo.domain.schedule.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
import kr.ai.nemo.global.testUtil.TestReflectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Schedule 도메인 테스트")
class ScheduleTest {

    @Test
    @DisplayName("[성공] Schedule 생성")
    void createSchedule_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(owner);
        String title = "테스트 스케줄";
        String description = "스케줄 설명";
        String address = "서울시 강남구";
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);

        // when
        Schedule schedule = Schedule.builder()
                .group(group)
                .owner(owner)
                .title(title)
                .description(description)
                .address(address)
                .currentUserCount(1)
                .status(ScheduleStatus.RECRUITING)
                .startAt(startAt)
                .build();

        // then
        assertThat(schedule.getGroup()).isEqualTo(group);
        assertThat(schedule.getOwner()).isEqualTo(owner);
        assertThat(schedule.getTitle()).isEqualTo(title);
        assertThat(schedule.getDescription()).isEqualTo(description);
        assertThat(schedule.getAddress()).isEqualTo(address);
        assertThat(schedule.getCurrentUserCount()).isEqualTo(1);
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.RECRUITING);
        assertThat(schedule.getStartAt()).isEqualTo(startAt);
    }

    @Test
    @DisplayName("[성공] 스케줄 취소")
    void cancel_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(owner);
        Schedule schedule = Schedule.builder()
                .group(group)
                .owner(owner)
                .title("취소할 스케줄")
                .status(ScheduleStatus.RECRUITING)
                .startAt(LocalDateTime.now().plusDays(1))
                .build();

        // when
        schedule.cancel();

        // then
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.CANCELED);
    }

    @Test
    @DisplayName("[성공] 현재 사용자 수 증가")
    void addCurrentUserCount_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(owner);
        Schedule schedule = Schedule.builder()
                .group(group)
                .owner(owner)
                .title("테스트 스케줄")
                .currentUserCount(3)
                .status(ScheduleStatus.RECRUITING)
                .startAt(LocalDateTime.now().plusDays(1))
                .build();

        // when
        schedule.addCurrentUserCount();

        // then
        assertThat(schedule.getCurrentUserCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("[성공] 현재 사용자 수 감소")
    void subtractCurrentUserCount_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(owner);
        Schedule schedule = Schedule.builder()
                .group(group)
                .owner(owner)
                .title("테스트 스케줄")
                .currentUserCount(5)
                .status(ScheduleStatus.RECRUITING)
                .startAt(LocalDateTime.now().plusDays(1))
                .build();

        // when
        schedule.subtractCurrentUserCount();

        // then
        assertThat(schedule.getCurrentUserCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("[성공] ID 설정")
    void setId_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(owner);
        Schedule schedule = Schedule.builder()
                .group(group)
                .owner(owner)
                .title("테스트 스케줄")
                .status(ScheduleStatus.RECRUITING)
                .startAt(LocalDateTime.now().plusDays(1))
                .build();

        Long id = 123L;

        // when
        TestReflectionUtils.setField(schedule, "id", id);

        // then
        assertThat(schedule.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("[성공] 상태 설정")
    void setStatus_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(owner);
        Schedule schedule = Schedule.builder()
                .group(group)
                .owner(owner)
                .title("테스트 스케줄")
                .status(ScheduleStatus.RECRUITING)
                .startAt(LocalDateTime.now().plusDays(1))
                .build();

        // when
        schedule.complete();

        // then
        assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.CLOSED);
    }

    @Test
    @DisplayName("[성공] 빌더 기본값 확인")
    void builderDefaults_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(owner);

        // when
        Schedule schedule = Schedule.builder()
                .group(group)
                .owner(owner)
                .title("테스트 스케줄")
                .status(ScheduleStatus.RECRUITING)
                .startAt(LocalDateTime.now().plusDays(1))
                .build();

        // then
        assertThat(schedule.getGroup()).isNotNull();
    }
}
