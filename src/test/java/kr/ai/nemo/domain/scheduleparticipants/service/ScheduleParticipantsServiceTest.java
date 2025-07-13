package kr.ai.nemo.domain.scheduleparticipants.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.schedule.validator.ScheduleValidator;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import kr.ai.nemo.domain.scheduleparticipants.domain.enums.ScheduleParticipantStatus;
import kr.ai.nemo.domain.scheduleparticipants.repository.ScheduleParticipantRepository;
import kr.ai.nemo.domain.scheduleparticipants.validator.ScheduleParticipantValidator;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.unit.global.fixture.group.GroupFixture;
import kr.ai.nemo.unit.global.fixture.schedule.ScheduleFixture;
import kr.ai.nemo.unit.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleParticipantsService 테스트")
class ScheduleParticipantsServiceTest {

    @Mock
    private ScheduleParticipantRepository scheduleParticipantRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private ScheduleValidator scheduleValidator;

    @Mock
    private ScheduleParticipantValidator scheduleParticipantValidator;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ScheduleParticipantsService scheduleParticipantsService;

    @Test
    @DisplayName("[성공] 그룹에 참여한 사용자를 향후 스케줄에 추가")
    void addParticipantToUpcomingSchedules_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        User newMember = UserFixture.createUser("newMember", "new@test.com", "kakao", "333");
        Group group = GroupFixture.createDefaultGroup(owner);

        Schedule schedule1 = ScheduleFixture.createScheduleWithId(1L, owner, group, "스케줄1");
        Schedule schedule2 = ScheduleFixture.createScheduleWithId(2L, owner, group, "스케줄2");
        List<Schedule> upcomingSchedules = Arrays.asList(schedule1, schedule2);

        given(scheduleRepository.findByGroupIdAndStatus(group.getId(), ScheduleStatus.RECRUITING))
                .willReturn(upcomingSchedules);
        given(scheduleParticipantRepository.existsByScheduleAndUser(schedule1, newMember))
                .willReturn(false);
        given(scheduleParticipantRepository.existsByScheduleAndUser(schedule2, newMember))
                .willReturn(false);

        // when
        scheduleParticipantsService.addParticipantToUpcomingSchedules(group, newMember);

        // then
        ArgumentCaptor<ScheduleParticipant> participantCaptor = ArgumentCaptor.forClass(ScheduleParticipant.class);
        verify(scheduleParticipantRepository, times(2)).save(participantCaptor.capture());

        List<ScheduleParticipant> savedParticipants = participantCaptor.getAllValues();
        assertThat(savedParticipants).hasSize(2);
        savedParticipants.forEach(p -> {
            assertThat(p.getUser()).isEqualTo(newMember);
            assertThat(p.getStatus()).isEqualTo(ScheduleParticipantStatus.PENDING);
        });
    }

    @Test
    @DisplayName("[성공] 참여 수락 - PENDING에서 ACCEPTED로")
    void decideParticipation_AcceptFromPending_Success() {
        // given
        Long scheduleId = 1L;
        Long userId = 1L;
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        Schedule schedule = ScheduleFixture.createScheduleWithId(scheduleId, user, group, "테스트 스케줄");
        
        ScheduleParticipant participant = ScheduleParticipant.builder()
                .schedule(schedule)
                .user(user)
                .status(ScheduleParticipantStatus.PENDING)
                .build();

        given(scheduleValidator.findByIdOrThrow(scheduleId)).willReturn(schedule);
        given(scheduleParticipantValidator.validateParticipationOrThrow(scheduleId, userId))
                .willReturn(participant);

        // when
        scheduleParticipantsService.decideParticipation(scheduleId, userId, ScheduleParticipantStatus.ACCEPTED);

        // then
        verify(scheduleValidator).validateScheduleStart(schedule);
        verify(scheduleParticipantValidator).validateStatusChange(
                ScheduleParticipantStatus.PENDING, ScheduleParticipantStatus.ACCEPTED);
        assertThat(participant.getStatus()).isEqualTo(ScheduleParticipantStatus.ACCEPTED);
    }

    @Test
    @DisplayName("[성공] 스케줄에 참가자가 없는 그룹")
    void addAllParticipantsForNewSchedule_NoParticipants_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(owner);

        Schedule schedule = ScheduleFixture.createDefaultSchedule(owner, group);

        // when
        scheduleParticipantsService.addAllParticipantsForNewSchedule(schedule);

        // then
        verify(scheduleParticipantRepository, never()).save(any(ScheduleParticipant.class));
    }
}
