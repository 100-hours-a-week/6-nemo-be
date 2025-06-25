package kr.ai.nemo.domain.groupparticipants.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.repository.GroupParticipantsRepository;
import kr.ai.nemo.domain.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.scheduleparticipants.service.ScheduleParticipantsService;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupParticipantsCommandService 테스트")
class GroupParticipantsCommandServiceTest {

  @Mock
  private GroupParticipantsRepository groupParticipantsRepository;

  @Mock
  private ScheduleParticipantsService scheduleParticipantsService;

  @Mock
  private GroupParticipantValidator groupParticipantValidator;

  @Mock
  private GroupValidator groupValidator;

  @Mock
  private UserRepository userRepository;

  @Mock
  private GroupRepository groupRepository;

  @InjectMocks
  private GroupParticipantsCommandService groupParticipantsCommandService;

  @Test
  @DisplayName("[성공] 모임 신청 테스트 (새가입)")
  void applyToGroup_Success() {
    // given
    Long groupId = 1L;
    User mockUser = UserFixture.createDefaultUser();
    userRepository.save(mockUser);

    Group mockGroup = mock(Group.class);
    groupRepository.save(mockGroup);

    CustomUserDetails userDetails = new CustomUserDetails(mockUser);
    Role mockRole = Role.MEMBER;
    Status mockStatus = Status.JOINED;

    // 이미 참여 이력이 없음 (Optional.empty)
    given(groupParticipantsRepository.findByGroupIdAndUserId(groupId, mockUser.getId()))
        .willReturn(Optional.empty());

    given(groupValidator.findByIdOrThrow(groupId)).willReturn(mockGroup);
    willDoNothing().given(groupValidator).validateGroupIsNotFull(mockGroup);
    willDoNothing().given(scheduleParticipantsService).addParticipantToUpcomingSchedules(mockGroup, mockUser);

    // when
    groupParticipantsCommandService.applyToGroup(groupId, userDetails, mockRole, mockStatus);

    // then
    verify(groupValidator).findByIdOrThrow(groupId);
    verify(groupValidator).validateGroupIsNotFull(mockGroup);
    verify(groupParticipantsRepository).save(any(GroupParticipants.class));
    verify(mockGroup).addCurrentUserCount();
    verify(scheduleParticipantsService).addParticipantToUpcomingSchedules(mockGroup, mockUser);
  }

  @Test
  @DisplayName("[성공] 모임 신청 테스트 (재가입)")
  void rejoinToGroup_Success() {
    // given
    Long groupId = 1L;
    User user = UserFixture.createDefaultUser();
    userRepository.save(user);

    Group group = GroupFixture.createDefaultGroup(user);
    groupRepository.save(group);

    CustomUserDetails userDetails = new CustomUserDetails(user);
    GroupParticipants participant = GroupParticipants.builder()
        .group(group)
        .user(user)
        .role(Role.MEMBER)
        .status(Status.KICKED)
        .appliedAt(LocalDateTime.now().minusDays(5))
        .build();

    groupParticipantsRepository.saveAndFlush(participant);

    // 이미 참여 이력이 있음
    given(groupParticipantsRepository.findByGroupIdAndUserId(groupId, user.getId()))
        .willReturn(Optional.of(participant));
    given(groupValidator.findByIdOrThrow(groupId)).willReturn(group);
    willDoNothing().given(groupValidator).validateGroupIsNotFull(group);
    willDoNothing().given(groupParticipantValidator).validateJoinedParticipant(participant);
    willDoNothing().given(scheduleParticipantsService).addParticipantToUpcomingSchedules(group, user);

    // when
    groupParticipantsCommandService.applyToGroup(groupId, userDetails, Role.MEMBER, Status.JOINED);

    // then
    verify(groupValidator).findByIdOrThrow(groupId);
    verify(groupValidator).validateGroupIsNotFull(group);
    verify(groupParticipantValidator).validateJoinedParticipant(participant);
    verify(scheduleParticipantsService).addParticipantToUpcomingSchedules(group, user);
    assertThat(participant.getStatus()).isEqualTo(Status.JOINED);
  }


  @Test
  @DisplayName("[성공] 모임원 추방 테스트")
  void kickOut_Success() {
    // given
    Long groupId = 1L;
    Long userId = 1L;

    User user = mock(User.class);
    Group group = GroupFixture.createDefaultGroup(user);
    groupRepository.saveAndFlush(group);
    group.setCurrentUserCount(5);

    CustomUserDetails userDetails = new CustomUserDetails(user);

    GroupParticipants mockParticipant = new GroupParticipants(
        1L,
        user,
        group,
        Role.MEMBER,
        Status.JOINED,
        LocalDateTime.now(),
        LocalDateTime.now(),
        null
    );

    willDoNothing().given(groupParticipantValidator).checkOwner(mockParticipant);

    given(groupValidator.isOwner(groupId, userDetails.getUserId()))
        .willReturn(group);
    given(groupParticipantValidator.getParticipant(anyLong(), anyLong()))
        .willReturn(mockParticipant);

    // when
    groupParticipantsCommandService.kickOut(groupId, userId, userDetails);

    // then
    assertThat(mockParticipant.getStatus()).isEqualTo(Status.KICKED);
    assertThat(group.getCurrentUserCount()).isEqualTo(4);
  }

  @Test
  @DisplayName("[성공] 모임 탈퇴 테스트")
  void withdrawGroup_Success() {
    // given
    Long groupId = 1L;
    User user = mock(User.class);
    CustomUserDetails userDetails = new CustomUserDetails(user);
    Group group = GroupFixture.createDefaultGroup(user);
    groupRepository.saveAndFlush(group);
    group.setCurrentUserCount(5);

    GroupParticipants mockParticipant = new GroupParticipants(
        1L,
        user,
        group,
        Role.MEMBER,
        Status.JOINED,
        LocalDateTime.now(),
        LocalDateTime.now(),
        null
    );

    willDoNothing().given(groupParticipantValidator).checkOwner(mockParticipant);

    given(groupValidator.findByIdOrThrow(groupId))
        .willReturn(group);
    given(groupParticipantValidator.getParticipant(anyLong(), anyLong()))
        .willReturn(mockParticipant);

    // when
    groupParticipantsCommandService.withdrawGroup(groupId, userDetails.getUserId());

    // then
    assertThat(group.getCurrentUserCount()).isEqualTo(4);
    assertThat(mockParticipant.getStatus()).isEqualTo(Status.WITHDRAWN);
    assertThat(mockParticipant.getDeletedAt()).isNotNull();
  }

  @Test
  @DisplayName("[성공] 그룹 신청 - 정원 체크")
  void applyToGroup_ValidateGroupCapacity_Success() {
    // given
    Long groupId = 1L;
    User user = UserFixture.createDefaultUser();
    Group group = GroupFixture.createDefaultGroup(user);
    CustomUserDetails userDetails = new CustomUserDetails(user);

    given(groupParticipantsRepository.findByGroupIdAndUserId(groupId, user.getId()))
        .willReturn(Optional.empty());
    given(groupValidator.findByIdOrThrow(groupId)).willReturn(group);
    doNothing().when(groupValidator).validateGroupIsNotFull(group);
    doNothing().when(scheduleParticipantsService).addParticipantToUpcomingSchedules(group, user);

    // when
    groupParticipantsCommandService.applyToGroup(groupId, userDetails, Role.MEMBER, Status.JOINED);

    // then
    verify(groupValidator).validateGroupIsNotFull(group);
    verify(groupParticipantsRepository).save(any(GroupParticipants.class));
  }

  @Test
  @DisplayName("[성공] 재가입 시 기존 참가자 검증")
  void rejoinToGroup_ValidateExistingParticipant_Success() {
    // given
    Long groupId = 1L;
    User user = UserFixture.createDefaultUser();
    Group group = GroupFixture.createDefaultGroup(user);
    CustomUserDetails userDetails = new CustomUserDetails(user);
    
    GroupParticipants existingParticipant = GroupParticipants.builder()
        .group(group)
        .user(user)
        .role(Role.MEMBER)
        .status(Status.WITHDRAWN)
        .appliedAt(LocalDateTime.now().minusDays(1))
        .build();

    given(groupParticipantsRepository.findByGroupIdAndUserId(groupId, user.getId()))
        .willReturn(Optional.of(existingParticipant));
    given(groupValidator.findByIdOrThrow(groupId)).willReturn(group);
    doNothing().when(groupValidator).validateGroupIsNotFull(group);
    doNothing().when(groupParticipantValidator).validateJoinedParticipant(existingParticipant);
    doNothing().when(scheduleParticipantsService).addParticipantToUpcomingSchedules(group, user);

    // when
    groupParticipantsCommandService.applyToGroup(groupId, userDetails, Role.MEMBER, Status.JOINED);

    // then
    verify(groupParticipantValidator).validateJoinedParticipant(existingParticipant);
    assertThat(existingParticipant.getStatus()).isEqualTo(Status.JOINED);
  }
}
