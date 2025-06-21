package kr.ai.nemo.domain.scheduleparticipants.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ScheduleParticipantStatus 테스트")
class ScheduleParticipantStatusTest {

    @Test
    @DisplayName("[성공] PENDING 상태 확인")
    void pending_Status_Test() {
        // given
        ScheduleParticipantStatus status = ScheduleParticipantStatus.PENDING;

        // when & then
        assertThat(status.isPending()).isTrue();
        assertThat(status.isAccepted()).isFalse();
    }

    @Test
    @DisplayName("[성공] ACCEPTED 상태 확인")
    void accepted_Status_Test() {
        // given
        ScheduleParticipantStatus status = ScheduleParticipantStatus.ACCEPTED;

        // when & then
        assertThat(status.isPending()).isFalse();
        assertThat(status.isAccepted()).isTrue();
    }

    @Test
    @DisplayName("[성공] REJECTED 상태 확인")
    void rejected_Status_Test() {
        // given
        ScheduleParticipantStatus status = ScheduleParticipantStatus.REJECTED;

        // when & then
        assertThat(status.isPending()).isFalse();
        assertThat(status.isAccepted()).isFalse();
    }

    @Test
    @DisplayName("[성공] 모든 enum 값 확인")
    void all_Enum_Values_Test() {
        // when
        ScheduleParticipantStatus[] values = ScheduleParticipantStatus.values();

        // then
        assertThat(values).hasSize(3);
        assertThat(values).containsExactly(
            ScheduleParticipantStatus.PENDING,
            ScheduleParticipantStatus.ACCEPTED,
            ScheduleParticipantStatus.REJECTED
        );
    }

    @Test
    @DisplayName("[성공] valueOf 테스트")
    void valueOf_Test() {
        // when & then
        assertThat(ScheduleParticipantStatus.valueOf("PENDING")).isEqualTo(ScheduleParticipantStatus.PENDING);
        assertThat(ScheduleParticipantStatus.valueOf("ACCEPTED")).isEqualTo(ScheduleParticipantStatus.ACCEPTED);
        assertThat(ScheduleParticipantStatus.valueOf("REJECTED")).isEqualTo(ScheduleParticipantStatus.REJECTED);
    }

    @Test
    @DisplayName("[성공] name() 테스트")
    void name_Test() {
        // when & then
        assertThat(ScheduleParticipantStatus.PENDING.name()).isEqualTo("PENDING");
        assertThat(ScheduleParticipantStatus.ACCEPTED.name()).isEqualTo("ACCEPTED");
        assertThat(ScheduleParticipantStatus.REJECTED.name()).isEqualTo("REJECTED");
    }

    @Test
    @DisplayName("[성공] ordinal() 테스트")
    void ordinal_Test() {
        // when & then
        assertThat(ScheduleParticipantStatus.PENDING.ordinal()).isEqualTo(0);
        assertThat(ScheduleParticipantStatus.ACCEPTED.ordinal()).isEqualTo(1);
        assertThat(ScheduleParticipantStatus.REJECTED.ordinal()).isEqualTo(2);
    }

    @Test
    @DisplayName("[성공] 상태 전환 로직 테스트")
    void status_Transition_Logic_Test() {
        // given
        ScheduleParticipantStatus pending = ScheduleParticipantStatus.PENDING;
        ScheduleParticipantStatus accepted = ScheduleParticipantStatus.ACCEPTED;
        ScheduleParticipantStatus rejected = ScheduleParticipantStatus.REJECTED;

        // when & then - PENDING에서만 다른 상태로 전환 가능하다고 가정
        assertThat(pending.isPending()).isTrue();
        
        // ACCEPTED나 REJECTED는 더 이상 PENDING이 아님
        assertThat(accepted.isPending()).isFalse();
        assertThat(rejected.isPending()).isFalse();
        
        // ACCEPTED만 isAccepted가 true
        assertThat(accepted.isAccepted()).isTrue();
        assertThat(pending.isAccepted()).isFalse();
        assertThat(rejected.isAccepted()).isFalse();
    }
}
