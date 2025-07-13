package kr.ai.nemo.domain.groupparticipants.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.group.service.GroupCacheService;
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
import kr.ai.nemo.global.testUtil.TestReflectionUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

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

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @Mock
  private GroupCacheService groupCacheService;

  @InjectMocks
  private GroupParticipantsCommandService groupParticipantsCommandService;

  @Test
  @DisplayName("[성공] 모임 신청 테스트 (새가입)")
  void applyToGroup_NewParticipant_Success() {
    // given
    Long groupId = 1L;
    Long userId = 1L; // 명시적 ID 설정

    User mockUser = UserFixture.createDefaultUser();
    TestReflectionUtils.setField(mockUser, "id", userId);

    Group mockGroup = GroupFixture.createDefaultGroup(mockUser);
    TestReflectionUtils.setField(mockGroup, "id", groupId);
    TestReflectionUtils.setField(mockGroup, "maxUserCount", 10);
    TestReflectionUtils.setField(mockGroup, "currentUserCount", 5);

    CustomUserDetails userDetails = new CustomUserDetails(mockUser);
    Role mockRole = Role.MEMBER;
    Status mockStatus = Status.JOINED;

    // Redis mocking - 캐시 미스 시나리오
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(anyString())).willReturn(null);
    willDoNothing().given(valueOperations).set(anyString(), anyString());
    given(valueOperations.increment(anyString())).willReturn(6L);

    // 이미 참여 이력이 없음 (Optional.empty) - 정확한 ID로 Mock 설정
    given(groupParticipantsRepository.findByGroupIdAndUserId(groupId, userId))
        .willReturn(Optional.empty());
    given(groupValidator.findByIdOrThrow(groupId)).willReturn(mockGroup);
    willDoNothing().given(scheduleParticipantsService).addParticipantToUpcomingSchedules(mockGroup, mockUser);
    willDoNothing().given(groupCacheService).evictGroupDetailStatic(groupId);
    willDoNothing().given(groupCacheService).deleteGroupListCaches();

    // when
    groupParticipantsCommandService.applyToGroup(groupId, userDetails, mockRole, mockStatus);

    // then
    verify(groupValidator).findByIdOrThrow(groupId);
    verify(groupParticipantsRepository).findByGroupIdAndUserId(groupId, userId);
    verify(groupParticipantsRepository).save(any(GroupParticipants.class));
    verify(scheduleParticipantsService).addParticipantToUpcomingSchedules(mockGroup, mockUser);
    verify(valueOperations).increment(anyString()); // 새 참가자이므로 카운트 증가
    verify(groupCacheService).evictGroupDetailStatic(groupId);
    verify(groupCacheService).deleteGroupListCaches();
  }

  @Test
  @DisplayName("[성공] 모임 신청 테스트 (재가입)")
  void applyToGroup_RejoinParticipant_Success() {
    // given
    Long groupId = 1L;
    Long userId = 1L; // 명시적 ID 설정

    User user = UserFixture.createDefaultUser();
    TestReflectionUtils.setField(user, "id", userId);

    Group group = GroupFixture.createDefaultGroup(user);
    TestReflectionUtils.setField(group, "id", groupId);
    TestReflectionUtils.setField(group, "maxUserCount", 10);
    TestReflectionUtils.setField(group, "currentUserCount", 5);

    CustomUserDetails userDetails = new CustomUserDetails(user);
    GroupParticipants participant = GroupParticipants.builder()
        .group(group)
        .user(user)
        .role(Role.MEMBER)
        .status(Status.KICKED)
        .appliedAt(LocalDateTime.now().minusDays(5))
        .build();

    // Redis mocking - 캐시 히트 시나리오
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(anyString())).willReturn("5");

    // 이미 참여 이력이 있음 - 정확한 ID로 Mock 설정
    given(groupParticipantsRepository.findByGroupIdAndUserId(groupId, userId))
        .willReturn(Optional.of(participant));
    given(groupValidator.findByIdOrThrow(groupId)).willReturn(group);
    willDoNothing().given(groupParticipantValidator).validateJoinedParticipant(participant);
    willDoNothing().given(scheduleParticipantsService).addParticipantToUpcomingSchedules(group, user);
    willDoNothing().given(groupCacheService).evictGroupDetailStatic(groupId);
    willDoNothing().given(groupCacheService).deleteGroupListCaches();

    // when
    groupParticipantsCommandService.applyToGroup(groupId, userDetails, Role.MEMBER, Status.JOINED);

    // then
    verify(groupValidator).findByIdOrThrow(groupId);
    verify(groupParticipantsRepository).findByGroupIdAndUserId(groupId, userId);
    verify(groupParticipantValidator).validateJoinedParticipant(participant);
    verify(scheduleParticipantsService).addParticipantToUpcomingSchedules(group, user);
    verify(valueOperations, never()).increment(anyString());
    verify(groupCacheService).evictGroupDetailStatic(groupId);
    verify(groupCacheService).deleteGroupListCaches();
    assertThat(participant.getStatus()).isEqualTo(Status.JOINED);
  }

  @Test
  @DisplayName("[실패] 모임 신청 - 정원 초과")
  void applyToGroup_GroupFull_ThrowException() {
    // given
    Long groupId = 1L;
    User mockUser = UserFixture.createDefaultUser();
    Group mockGroup = GroupFixture.createDefaultGroup(mockUser);
    TestReflectionUtils.setField(mockGroup, "maxUserCount", 5);
    TestReflectionUtils.setField(mockGroup, "currentUserCount", 5);

    CustomUserDetails userDetails = new CustomUserDetails(mockUser);

    // Redis mocking - 정원 가득 찬 상황
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(anyString())).willReturn("5");
    given(groupValidator.findByIdOrThrow(groupId)).willReturn(mockGroup);

    // when & then
    assertThatThrownBy(() ->
        groupParticipantsCommandService.applyToGroup(groupId, userDetails, Role.MEMBER, Status.JOINED))
        .isInstanceOf(GroupException.class)
        .hasFieldOrPropertyWithValue("errorCode", GroupErrorCode.GROUP_FULL);

    verify(groupParticipantsRepository, never()).save(any(GroupParticipants.class));
    verify(scheduleParticipantsService, never()).addParticipantToUpcomingSchedules(any(), any());
  }

  @Test
  @DisplayName("[성공] 그룹 리더 생성")
  void createToGroupLeader_Success() {
    // given
    User user = UserFixture.createDefaultUser();
    Group group = GroupFixture.createDefaultGroup(user);
    TestReflectionUtils.setField(group, "currentUserCount", 0);

    // when
    groupParticipantsCommandService.createToGroupLeader(group, user);

    // then
    verify(groupParticipantsRepository).save(any(GroupParticipants.class));
    assertThat(group.getCurrentUserCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("[성공] 모임원 추방 테스트")
  void kickOut_Success() {
    // given
    Long groupId = 1L;
    Long userId = 2L; // 다른 사용자 ID

    User owner = UserFixture.createDefaultUser();
    TestReflectionUtils.setField(owner, "id", 1L);

    User targetUser = UserFixture.createDefaultUser();
    TestReflectionUtils.setField(targetUser, "id", userId);

    Group group = GroupFixture.createDefaultGroup(owner);
    TestReflectionUtils.setField(group, "currentUserCount", 5);

    CustomUserDetails ownerDetails = new CustomUserDetails(owner);

    GroupParticipants mockParticipant = GroupParticipants.builder()
        .user(targetUser)
        .group(group)
        .role(Role.MEMBER)
        .status(Status.JOINED)
        .appliedAt(LocalDateTime.now())
        .build();

    given(groupValidator.isOwner(groupId, ownerDetails.getUserId()))
        .willReturn(group);
    given(groupParticipantValidator.getParticipant(groupId, userId))
        .willReturn(mockParticipant);
    willDoNothing().given(groupParticipantValidator).checkOwner(mockParticipant);
    willDoNothing().given(groupCacheService).evictGroupDetailStatic(groupId);
    willDoNothing().given(groupCacheService).deleteGroupListCaches();

    // when
    groupParticipantsCommandService.kickOut(groupId, userId, ownerDetails);

    // then
    assertThat(mockParticipant.getStatus()).isEqualTo(Status.KICKED);
    assertThat(group.getCurrentUserCount()).isEqualTo(4);
    verify(groupCacheService).evictGroupDetailStatic(groupId);
    verify(groupCacheService).deleteGroupListCaches();
  }

  @Test
  @DisplayName("[성공] 모임 탈퇴 테스트")
  void withdrawGroup_Success() {
    // given
    Long groupId = 1L;
    User user = UserFixture.createDefaultUser();
    TestReflectionUtils.setField(user, "id", 1L);

    Group group = GroupFixture.createDefaultGroup(user);
    TestReflectionUtils.setField(group, "currentUserCount", 5);

    GroupParticipants mockParticipant = GroupParticipants.builder()
        .user(user)
        .group(group)
        .role(Role.MEMBER)
        .status(Status.JOINED)
        .appliedAt(LocalDateTime.now())
        .build();

    given(groupValidator.findByIdOrThrow(groupId))
        .willReturn(group);
    given(groupParticipantValidator.getParticipant(groupId, user.getId()))
        .willReturn(mockParticipant);
    willDoNothing().given(groupParticipantValidator).checkOwner(mockParticipant);
    willDoNothing().given(groupCacheService).evictGroupDetailStatic(groupId);
    willDoNothing().given(groupCacheService).deleteGroupListCaches();

    // when
    groupParticipantsCommandService.withdrawGroup(groupId, user.getId());

    // then
    assertThat(group.getCurrentUserCount()).isEqualTo(4);
    assertThat(mockParticipant.getStatus()).isEqualTo(Status.WITHDRAWN);
    verify(groupCacheService).evictGroupDetailStatic(groupId);
    verify(groupCacheService).deleteGroupListCaches();
  }

  @Test
  @DisplayName("[성공] Redis 캐시 사용 - 기존 카운트 존재")
  void applyToGroup_WithCachedCount_Success() {
    // given
    Long groupId = 1L;
    Long userId = 1L; // 명시적 ID 설정

    User mockUser = UserFixture.createDefaultUser();
    TestReflectionUtils.setField(mockUser, "id", userId);

    Group mockGroup = GroupFixture.createDefaultGroup(mockUser);
    TestReflectionUtils.setField(mockGroup, "id", groupId);
    TestReflectionUtils.setField(mockGroup, "maxUserCount", 10);
    TestReflectionUtils.setField(mockGroup, "currentUserCount", 3);

    CustomUserDetails userDetails = new CustomUserDetails(mockUser);

    // Redis mocking - 캐시 히트 시나리오
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(anyString())).willReturn("3"); // 캐시된 값
    given(valueOperations.increment(anyString())).willReturn(4L);

    given(groupParticipantsRepository.findByGroupIdAndUserId(groupId, userId))
        .willReturn(Optional.empty());
    given(groupValidator.findByIdOrThrow(groupId)).willReturn(mockGroup);
    willDoNothing().given(scheduleParticipantsService).addParticipantToUpcomingSchedules(mockGroup, mockUser);
    willDoNothing().given(groupCacheService).evictGroupDetailStatic(groupId);
    willDoNothing().given(groupCacheService).deleteGroupListCaches();

    // when
    groupParticipantsCommandService.applyToGroup(groupId, userDetails, Role.MEMBER, Status.JOINED);

    // then
    verify(valueOperations).get(anyString()); // 캐시 조회
    verify(valueOperations, never()).set(anyString(), anyString());
    verify(valueOperations).increment(anyString()); // 카운트 증가
    verify(groupParticipantsRepository).findByGroupIdAndUserId(groupId, userId);
    verify(groupParticipantsRepository).save(any(GroupParticipants.class));
  }

  @Test
  @DisplayName("[성공] Redis 캐시 미스 - 새로운 카운트 설정")
  void applyToGroup_CacheMiss_SetNewCount_Success() {
    // given
    Long groupId = 1L;
    Long userId = 1L; // 명시적 ID 설정

    User mockUser = UserFixture.createDefaultUser();
    TestReflectionUtils.setField(mockUser, "id", userId);

    Group mockGroup = GroupFixture.createDefaultGroup(mockUser);
    TestReflectionUtils.setField(mockGroup, "id", groupId);
    TestReflectionUtils.setField(mockGroup, "maxUserCount", 10);
    TestReflectionUtils.setField(mockGroup, "currentUserCount", 2);

    CustomUserDetails userDetails = new CustomUserDetails(mockUser);

    // Redis mocking - 캐시 미스 시나리오
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(anyString())).willReturn(null); // 캐시 미스
    willDoNothing().given(valueOperations).set(anyString(), eq("2")); // 현재 그룹 카운트로 설정
    given(valueOperations.increment(anyString())).willReturn(3L);

    given(groupParticipantsRepository.findByGroupIdAndUserId(groupId, userId))
        .willReturn(Optional.empty());
    given(groupValidator.findByIdOrThrow(groupId)).willReturn(mockGroup);
    willDoNothing().given(scheduleParticipantsService).addParticipantToUpcomingSchedules(mockGroup, mockUser);
    willDoNothing().given(groupCacheService).evictGroupDetailStatic(groupId);
    willDoNothing().given(groupCacheService).deleteGroupListCaches();

    // when
    groupParticipantsCommandService.applyToGroup(groupId, userDetails, Role.MEMBER, Status.JOINED);

    // then
    verify(valueOperations).get(anyString()); // 캐시 조회
    verify(valueOperations).set(anyString(), eq("2")); // 현재 카운트로 캐시 설정
    verify(valueOperations).increment(anyString()); // 카운트 증가
    verify(groupParticipantsRepository).findByGroupIdAndUserId(groupId, userId); // ⭐ 정확한 ID 검증
    verify(groupParticipantsRepository).save(any(GroupParticipants.class));
  }
}
