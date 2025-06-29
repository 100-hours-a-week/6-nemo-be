package kr.ai.nemo.domain.schedule.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.will;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.dto.request.ScheduleCreateRequest;
import kr.ai.nemo.domain.schedule.dto.response.ScheduleCreateResponse;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.schedule.validator.ScheduleValidator;
import kr.ai.nemo.domain.scheduleparticipants.service.ScheduleParticipantsService;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.schedule.ScheduleFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
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

  @InjectMocks
  private ScheduleCommandService scheduleCommandService;

  @Test
  @DisplayName("[성공] 일정 생성 테스트")
  void createSchedule_Success() {
    // given
    User user = UserFixture.createDefaultUser();
    Group group = GroupFixture.createDefaultGroup(user);
    CustomUserDetails userDetails = new CustomUserDetails(user);
    
    LocalDateTime startAt = LocalDateTime.now().plusDays(7);
    LocalDateTime beforeUpdate = group.getUpdatedAt();

    ScheduleCreateRequest request = new ScheduleCreateRequest(
        1L,
        "일정 제목",
        "일정 내용",
        "일정 주소",
        "일정 상세 주소",
        startAt
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
    willDoNothing().given(scheduleParticipantsService).addAllParticipantsForNewSchedule(any(Schedule.class));

    // when
    ScheduleCreateResponse response = scheduleCommandService.createSchedule(userDetails, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.title()).isEqualTo(request.title());
    assertThat(response.description()).isEqualTo(request.description());
    assertThat(response.address()).isEqualTo(request.fullAddress());
    
    // 그룹의 업데이트 시간이 변경되었는지 확인
    assertThat(group.getUpdatedAt()).isAfter(beforeUpdate);

    verify(groupValidator).findByIdOrThrow(request.groupId());
    verify(scheduleRepository).save(any(Schedule.class));
    verify(scheduleParticipantsService).addAllParticipantsForNewSchedule(any(Schedule.class));
  }

  @Test
  @DisplayName("[성공] 일정 삭제 테스트")
  void deleteSchedule_Success() {
    // given
    Long scheduleId = 1L;
    User user = UserFixture.createDefaultUser();
    Group group = GroupFixture.createDefaultGroup(user);

    Schedule schedule = ScheduleFixture.createDefaultSchedule(user, group);

    given(scheduleValidator.findByIdOrThrow(scheduleId)).willReturn(schedule);
    willDoNothing().given(scheduleValidator).validateSchedule(schedule);

    // when
    scheduleCommandService.deleteSchedule(scheduleId);

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
    Group group = GroupFixture.createDefaultGroup(UserFixture.createDefaultUser());
    User user = UserFixture.createDefaultUser();
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
    given(scheduleRepository.save(any(Schedule.class))).willAnswer(invocation -> invocation.getArgument(0));
    willDoNothing().given(scheduleParticipantsService).addAllParticipantsForNewSchedule(any(Schedule.class));

    // when
    scheduleCommandService.createSchedule(userDetails, request);

    // then
    assertThat(group.getUpdatedAt()).isAfter(beforeCreation);
    verify(scheduleParticipantsService).addAllParticipantsForNewSchedule(any(Schedule.class));
  }

  @Test
  @DisplayName("[실패] 일정 생성 - 존재하지 않는 그룹")
  void createSchedule_GroupNotFound_ThrowException() {
    // given
    User user = UserFixture.createDefaultUser();
    CustomUserDetails userDetails = new CustomUserDetails(user);

    ScheduleCreateRequest request = new ScheduleCreateRequest(
        999L, // 존재하지 않는 그룹 ID
        "팀 미팅",
        "월례 팀 미팅입니다.",
        "서울시 강남구",
        "테헤란로 123",
        LocalDateTime.now().plusDays(7)
    );

    given(groupValidator.findByIdOrThrow(999L))
        .willThrow(new GroupException(kr.ai.nemo.domain.group.exception.GroupErrorCode.GROUP_NOT_FOUND));

    // when & then
    assertThatThrownBy(() -> scheduleCommandService.createSchedule(userDetails, request))
        .isInstanceOf(GroupException.class);

    verify(groupValidator).findByIdOrThrow(999L);
  }

  @Test
  @DisplayName("[성공] 일정 생성 - 긴 제목과 설명")
  void createSchedule_LongTitleAndDescription_Success() {
    // given
    User user = UserFixture.createDefaultUser();
    Group group = GroupFixture.createDefaultGroup(user);
    CustomUserDetails userDetails = new CustomUserDetails(user);

    String longTitle = "매우 긴 제목입니다. ".repeat(10);
    String longDescription = "매우 긴 설명입니다. ".repeat(20);

    ScheduleCreateRequest request = new ScheduleCreateRequest(
        1L,
        longTitle,
        longDescription,
        "서울시 강남구",
        "테헤란로 123",
        LocalDateTime.now().plusDays(7)
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

    given(groupValidator.findByIdOrThrow(anyLong())).willReturn(group);
    given(scheduleRepository.save(any(Schedule.class))).willReturn(mockSchedule);
    willDoNothing().given(scheduleParticipantsService)
        .addAllParticipantsForNewSchedule(any(Schedule.class));

    // when
    ScheduleCreateResponse response = scheduleCommandService.createSchedule(userDetails, request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.title()).isEqualTo(longTitle);
    assertThat(response.description()).isEqualTo(longDescription);
  }
}
