package kr.ai.nemo.domain.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.dto.response.MySchedulesResponse;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleDetailResponse;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleListResponse;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.schedule.validator.ScheduleValidator;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import kr.ai.nemo.domain.scheduleparticipants.domain.enums.ScheduleParticipantStatus;
import kr.ai.nemo.domain.scheduleparticipants.repository.ScheduleParticipantRepository;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.schedule.ScheduleFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleQueryService 테스트")
class ScheduleQueryServiceTest {

  @Mock
  private ScheduleRepository scheduleRepository;

  @Mock
  private ScheduleParticipantRepository scheduleParticipantRepository;

  @Mock
  private GroupValidator groupValidator;

  @Mock
  private ScheduleValidator scheduleValidator;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private ScheduleQueryService scheduleQueryService;

  @Test
  @DisplayName("[성공] 일정 상세 조회 테스트")
  void getScheduleDetail_Success() {
    // given
    Long scheduleId = 1L;
    User user = UserFixture.createDefaultUser();
    Group group = GroupFixture.createDefaultGroup(user);

    Schedule schedule = ScheduleFixture.createDefaultSchedule(user, group);
    List<ScheduleParticipant> participants = List.of(
        ScheduleParticipant.builder()
          .id(1L)
          .schedule(schedule)
          .user(user)
          .status(ScheduleParticipantStatus.ACCEPTED)
          .build()
    );

    willReturn(schedule).given(scheduleValidator).findByIdOrThrow(scheduleId);
    willReturn(participants).given(scheduleParticipantRepository).findByScheduleId(scheduleId);

    // when
    ScheduleDetailResponse response = scheduleQueryService.getScheduleDetail(scheduleId);

    // then
    assertThat(response.group().name()).isEqualTo(group.getName());
    assertThat(response.participants().getFirst().user().nickname()).isEqualTo(user.getNickname());
  }

  @Test
  @DisplayName("[성공] 모임별 일정 조회 테스트")
  void getGroupSchedules_Success() {
    // given
    Long groupId = 1L;
    PageRequest pageRequest = PageRequest.of(0, 10);

    User user = mock(User.class);
    Group group = GroupFixture.createDefaultGroup(user);

    Schedule schedule = ScheduleFixture.createDefaultSchedule(user, group);

    Page<Schedule> page = new PageImpl<>(List.of(schedule));

    willReturn(group).given(groupValidator).findByIdOrThrow(groupId);
    given(scheduleRepository.findByGroupIdAndStatusNot(groupId, pageRequest, ScheduleStatus.CANCELED)).willReturn(page);

    // when
    ScheduleListResponse response = scheduleQueryService.getGroupSchedules(groupId, pageRequest);

    // then
    assertNotNull(response);
    assertThat(response.schedules()).hasSize(1);

    ScheduleListResponse.ScheduleSummary summary = response.schedules().getFirst();
    assertThat(summary.title()).isEqualTo(schedule.getTitle());
    assertThat(summary.ownerName()).isEqualTo(schedule.getOwner().getNickname());
  }

  @Test
  @DisplayName("[성공] 내 스케줄 조회 테스트")
  void getMySchedules_Success() {
    // given
    Long userId = 1L;

    // when
    MySchedulesResponse response = scheduleQueryService.getMySchedules(userId);

    // then
  }
}
