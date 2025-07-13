package kr.ai.nemo.domain.groupparticipants.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.dto.response.GroupParticipantsListResponse;
import kr.ai.nemo.domain.groupparticipants.dto.response.MyGroupDto;
import kr.ai.nemo.domain.groupparticipants.repository.GroupParticipantsRepository;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.unit.global.fixture.group.GroupFixture;
import kr.ai.nemo.unit.global.fixture.user.UserFixture;
import kr.ai.nemo.unit.global.testUtil.TestReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupParticipantsQueryService 테스트")
class GroupParticipantsQueryServiceTest {

  @Mock
  private GroupParticipantsRepository groupParticipantsRepository;

  @Mock
  private GroupValidator groupValidator;

  @Mock
  private UserRepository userRepository;

  @Mock
  private GroupRepository groupRepository;

  @InjectMocks
  private GroupParticipantsQueryService groupParticipantsQueryService;

  Group group;
  User user;
  Long groupId;
  GroupParticipants participant;

  @BeforeEach
  void setUp() {
    user = UserFixture.createDefaultUser();

    group = GroupFixture.createDefaultGroup(user);
    TestReflectionUtils.setField(group, "id", 1L);
    groupId = 1L;

     participant = GroupParticipants.builder()
        .user(user)
        .group(group)
        .role(Role.MEMBER)
        .status(Status.JOINED)
        .appliedAt(LocalDateTime.now())
        .build();

    groupParticipantsRepository.save(participant);

  }

  @Test
  @DisplayName("[성공] 모임원 list 조회 테스트")
  void getGroupParticipants_Success() {
    // given
    given(groupValidator.findByIdOrThrow(groupId)).willReturn(group);
    given(groupParticipantsRepository.findByGroupIdAndStatus(groupId, Status.JOINED))
        .willReturn(List.of(participant));

    // when
    List<GroupParticipantsListResponse.GroupParticipantDto> result =
        groupParticipantsQueryService.getAcceptedParticipants(groupId);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isEqualTo(
        GroupParticipantsListResponse.GroupParticipantDto.from(participant));

    verify(groupValidator).findByIdOrThrow(groupId);
    verify(groupParticipantsRepository).findByGroupIdAndStatus(groupId, Status.JOINED);
  }

  @Test
  @DisplayName("나의 모임 list 조회 테스트")
  void getMyGroup_Success() {
    // given
    given(groupParticipantsRepository.findByUserIdAndStatus(user.getId(), Status.JOINED))
        .willReturn(List.of(participant));

    // when
    List<MyGroupDto> result = groupParticipantsQueryService.getMyGroups(user.getId());

    // then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().groupId()).isEqualTo(groupId);
    verify(groupParticipantsRepository).findByUserIdAndStatus(user.getId(), Status.JOINED);
  }
}
