package kr.ai.nemo.domain.scheduleparticipants.domain;

import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.scheduleparticipants.domain.enums.ScheduleParticipantStatus;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.user.UserFixture;
import kr.ai.nemo.global.testUtil.TestReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ScheduleParticipant 도메인 테스트")
class ScheduleParticipantTest {

    private User testUser;
    private Schedule testSchedule;

    @BeforeEach
    void setUp() {
        testUser = UserFixture.createDefaultUser();

        testSchedule = Schedule.builder()
                .title("테스트 일정")
                .build();
    }

    @Test
    @DisplayName("[성공] ScheduleParticipant 생성 테스트")
    void createScheduleParticipant() {
        // given
        ScheduleParticipantStatus status = ScheduleParticipantStatus.PENDING;

        // when
        ScheduleParticipant participant = ScheduleParticipant.builder()
                .schedule(testSchedule)
                .user(testUser)
                .status(status)
                .build();

        // then
        assertThat(participant.getSchedule()).isEqualTo(testSchedule);
        assertThat(participant.getUser()).isEqualTo(testUser);
        assertThat(participant.getStatus()).isEqualTo(status);
        assertThat(participant.getJoinedAt()).isNull();
    }

    @Test
    @DisplayName("[성공] 스케줄 참가 승낙 테스트")
    void accept() {
        // given
        ScheduleParticipant participant = ScheduleParticipant.builder()
                .schedule(testSchedule)
                .user(testUser)
                .status(ScheduleParticipantStatus.PENDING)
                .build();

        LocalDateTime beforeAccept = LocalDateTime.now();

        // when
        participant.accept();

        // then
        assertThat(participant.getStatus()).isEqualTo(ScheduleParticipantStatus.ACCEPTED);
        assertThat(participant.getJoinedAt()).isNotNull();
        assertThat(participant.getJoinedAt()).isAfter(beforeAccept);
    }

    @Test
    @DisplayName("[성공] 스케줄 참가 거절 테스트")
    void reject() {
        // given
        ScheduleParticipant participant = ScheduleParticipant.builder()
                .schedule(testSchedule)
                .user(testUser)
                .status(ScheduleParticipantStatus.PENDING)
                .joinedAt(LocalDateTime.now())
                .build();

        // when
        participant.reject();

        // then
        assertThat(participant.getStatus()).isEqualTo(ScheduleParticipantStatus.REJECTED);
        assertThat(participant.getJoinedAt()).isNull();
    }

    @Test
    @DisplayName("[성공] Setter 메서드 테스트")
    void setterMethods() {
        // given
        ScheduleParticipant participant = ScheduleParticipant.builder()
                .schedule(testSchedule)
                .user(testUser)
                .status(ScheduleParticipantStatus.PENDING)
                .build();

        LocalDateTime newJoinedAt = LocalDateTime.now();
        ScheduleParticipantStatus newStatus = ScheduleParticipantStatus.ACCEPTED;

        // when
        participant.setStatus(newStatus);
        participant.setJoinedAt(newJoinedAt);

        // then
        assertThat(participant.getStatus()).isEqualTo(newStatus);
        assertThat(participant.getJoinedAt()).isEqualTo(newJoinedAt);
    }

    @Test
    @DisplayName("[성공] @PrePersist 테스트")
    void prePersist() {
        // given
        ScheduleParticipant participant = ScheduleParticipant.builder()
                .schedule(testSchedule)
                .user(testUser)
                .status(ScheduleParticipantStatus.PENDING)
                .build();

        // when
        TestReflectionUtils.callMethod(participant, "onCreate");

        // then
        assertThat(participant.getCreatedAt()).isNotNull();
        assertThat(participant.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("[성공] @PreUpdate 테스트")
    void preUpdate() {
        // given
        ScheduleParticipant participant = ScheduleParticipant.builder()
                .schedule(testSchedule)
                .user(testUser)
                .status(ScheduleParticipantStatus.PENDING)
                .build();

        // onCreate 호출하여 createdAt, updatedAt 설정
        TestReflectionUtils.callMethod(participant, "onCreate");
        LocalDateTime createdAt = participant.getCreatedAt();

        // 약간의 시간 지연을 위해 sleep 추가
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // when
        TestReflectionUtils.callMethod(participant, "onUpdate");

        // then
        assertThat(participant.getUpdatedAt()).isNotNull();
        assertThat(participant.getUpdatedAt()).isAfter(createdAt);
        assertThat(participant.getCreatedAt()).isEqualTo(createdAt); // createdAt은 변경되지 않음
    }

    @Test
    @DisplayName("[성공] AllArgsConstructor 테스트")
    void allArgsConstructor() {
        // given
        Long id = 1L;
        ScheduleParticipantStatus status = ScheduleParticipantStatus.ACCEPTED;
        LocalDateTime joinedAt = LocalDateTime.now();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        // when
        ScheduleParticipant participant = new ScheduleParticipant(
                id, testSchedule, testUser, status, joinedAt, createdAt, updatedAt
        );

        // then
        assertThat(participant.getId()).isEqualTo(id);
        assertThat(participant.getSchedule()).isEqualTo(testSchedule);
        assertThat(participant.getUser()).isEqualTo(testUser);
        assertThat(participant.getStatus()).isEqualTo(status);
        assertThat(participant.getJoinedAt()).isEqualTo(joinedAt);
        assertThat(participant.getCreatedAt()).isEqualTo(createdAt);
        assertThat(participant.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("[성공] NoArgsConstructor 테스트")
    void noArgsConstructor() {
        // when
        ScheduleParticipant participant = new ScheduleParticipant();

        // then
        assertThat(participant).isNotNull();
        assertThat(participant.getId()).isNull();
        assertThat(participant.getSchedule()).isNull();
        assertThat(participant.getUser()).isNull();
        assertThat(participant.getStatus()).isNull();
        assertThat(participant.getJoinedAt()).isNull();
        assertThat(participant.getCreatedAt()).isNull();
        assertThat(participant.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("[성공] 상태 변경 시나리오 테스트")
    void statusChangeScenarios() {
        // given
        ScheduleParticipant participant = ScheduleParticipant.builder()
                .schedule(testSchedule)
                .user(testUser)
                .status(ScheduleParticipantStatus.PENDING)
                .build();

        // when & then - PENDING -> ACCEPTED
        participant.accept();
        assertThat(participant.getStatus()).isEqualTo(ScheduleParticipantStatus.ACCEPTED);
        assertThat(participant.getJoinedAt()).isNotNull();

        // when & then - ACCEPTED -> REJECTED
        participant.reject();
        assertThat(participant.getStatus()).isEqualTo(ScheduleParticipantStatus.REJECTED);
        assertThat(participant.getJoinedAt()).isNull();

        // when & then - REJECTED -> ACCEPTED
        participant.accept();
        assertThat(participant.getStatus()).isEqualTo(ScheduleParticipantStatus.ACCEPTED);
        assertThat(participant.getJoinedAt()).isNotNull();
    }
}
