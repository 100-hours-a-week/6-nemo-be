package kr.ai.nemo.domain.groupparticipants.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
  @DisplayName("[성공] 모임 신청 테스트")
  void applyToGroup_Success() {
    // given
    Long groupId = 1L;
    User mockUser = UserFixture.createDefaultUser();
    userRepository.save(mockUser);

    Group mockGroup = mock(Group.class);
    groupRepository.save(mockGroup);

    Role mockRole = mock(Role.class);
    Status mockStatus = mock(Status.class);

    CustomUserDetails userDetails = new CustomUserDetails(mockUser);
    when(groupValidator.findByIdOrThrow(groupId)).thenReturn(mockGroup);
    willDoNothing().given(groupParticipantValidator).validateJoinedParticipant(groupId, mockUser.getId());
    willDoNothing().given(groupValidator).validateGroupIsNotFull(any(Group.class));
    willDoNothing().given(scheduleParticipantsService).addParticipantToUpcomingSchedules(mockGroup, mockUser);

    // when
    groupParticipantsCommandService.applyToGroup(groupId, userDetails, mockRole, mockStatus);

    // then
    verify(groupValidator).findByIdOrThrow(groupId);
    verify(groupParticipantValidator).validateJoinedParticipant(groupId, mockUser.getId());
    verify(groupValidator).validateGroupIsNotFull(any(Group.class));
    verify(groupParticipantsRepository).save(any(GroupParticipants.class));
    verify(mockGroup).addCurrentUserCount();
    verify(scheduleParticipantsService).addParticipantToUpcomingSchedules(mockGroup, mockUser);
  }
}
