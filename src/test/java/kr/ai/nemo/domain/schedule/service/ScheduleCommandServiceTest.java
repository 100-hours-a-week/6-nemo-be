package kr.ai.nemo.domain.schedule.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.dto.request.ScheduleCreateRequest;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleCreateResponse;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.schedule.validator.ScheduleValidator;
import kr.ai.nemo.domain.scheduleparticipants.service.ScheduleParticipantsService;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.global.fixture.schedule.ScheduleFixture;
import kr.ai.nemo.global.testUtil.TestReflectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleCommandService 테스트")
class ScheduleCommandServiceTest {

  @Mock
  private ScheduleRepository scheduleRepository;

  @Mock
  private ScheduleParticipantsService scheduleParticipantsService;

  @Mock
  private GroupValidator groupValidator;

  @Mock
  private ScheduleValidator scheduleValidator;

  @Mock
  private UserRepository userRepository;

  @Mock
  private GroupRepository groupRepository;

  @InjectMocks
  private ScheduleCommandService scheduleCommandService;

  @Test
  @DisplayName("[성공] 일정 생성 테스트")
  void createSchedule_Success() {
    // given
    Group group = mock(Group.class);
    User user = mock(User.class);
    CustomUserDetails userDetails = new CustomUserDetails(user);

    ScheduleCreateRequest request = new ScheduleCreateRequest(
        1L,
        "일정 제목",
        "일정 내용",
        "일정 주소",
        "일정 상세 주소",
        LocalDateTime.now()
    );

    Schedule mockSchedule = Schedule.builder()
        .group(group)
        .owner(user)
        .title(request.title())
        .description(request.description())
        .address(request.fullAddress())
        .currentUserCount(1)
        .status(ScheduleStatus.RECRUITING)
        .startAt(request.startAt())
        .build();

    TestReflectionUtils.setField(mockSchedule, "createdAt", LocalDateTime.now());

    given(groupValidator.findByIdOrThrow(anyLong())).willReturn(group);
    given(scheduleRepository.save(any(Schedule.class))).willReturn(mockSchedule);
    doNothing().when(scheduleParticipantsService)
        .addAllParticipantsForNewSchedule(any(Schedule.class));

    // when
    ScheduleCreateResponse response = scheduleCommandService.createSchedule(userDetails, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.title()).isEqualTo(request.title());
    assertThat(response.description()).isEqualTo(request.description());
    assertThat(response.address()).isEqualTo(request.fullAddress());
  }

  @Test
  @DisplayName("[성공] 일정 삭제 테스트")
  void deleteSchedule_Success() {
    // given
    Long scheduleId = 1L;
    Group group = mock(Group.class);
    User user = mock(User.class);

    Schedule schedule = ScheduleFixture.createDefaultSchedule(user, group);

    when(scheduleValidator.findByIdOrThrow(scheduleId)).thenReturn(schedule);
    doNothing().when(scheduleValidator).validateSchedule(schedule);

    // when
    scheduleCommandService.deleteSchedule(1L);

    // then
    verify(scheduleValidator).findByIdOrThrow(scheduleId);
    verify(scheduleValidator).validateSchedule(schedule);
    assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.CANCELED);
    assertThat(schedule.getDeletedAt()).isNotNull();
  }

  @Test
  @DisplayName("[성공] 스케줄 생성 시 그룹 업데이트 시간 변경")
  void createSchedule_UpdatesGroupTime_Success() {
    // given
    Group group = mock(Group.class);
    User user = mock(User.class);
    CustomUserDetails userDetails = new CustomUserDetails(user);
    
    LocalDateTime beforeCreation = LocalDateTime.now();
    ScheduleCreateRequest request = new ScheduleCreateRequest(
        1L,
        "시간 테스트 스케줄",
        "설명",
        "주소",
        "상세주소",
        LocalDateTime.now().plusDays(1)
    );

    given(groupValidator.findByIdOrThrow(1L)).willReturn(group);
    // scheduleRepository.save()는 반환값을 사용하지 않으므로 설정 불필요

    // when
    scheduleCommandService.createSchedule(userDetails, request);

    // then
    verify(group).setUpdatedAt(any(LocalDateTime.class));
    verify(scheduleParticipantsService).addAllParticipantsForNewSchedule(any(Schedule.class));
  }
}
