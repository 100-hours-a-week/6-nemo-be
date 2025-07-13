package kr.ai.nemo.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.dto.request.GroupCreateRequest;
import kr.ai.nemo.domain.group.dto.request.UpdateGroupImageRequest;
import kr.ai.nemo.domain.group.dto.response.GroupCreateResponse;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.repository.GroupParticipantsRepository;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsCommandService;
import kr.ai.nemo.domain.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.unit.global.fixture.group.GroupFixture;
import kr.ai.nemo.unit.global.fixture.user.UserFixture;
import kr.ai.nemo.unit.global.redis.RedisCacheService;
import kr.ai.nemo.unit.global.testUtil.TestReflectionUtils;
import kr.ai.nemo.infra.ImageService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupCommandService 테스트")
class GroupCommandServiceTest {

  @Mock
  private GroupRepository groupRepository;

  @Mock
  private GroupParticipantsRepository groupParticipantsRepository;

  @Mock
  private ScheduleRepository scheduleRepository;

  @Mock
  private GroupTagService groupTagService;

  @Mock
  private GroupParticipantsCommandService groupParticipantsCommandService;

  @Mock
  private GroupCacheService groupCacheService;

  @Mock
  private AiGroupService aiClient;

  @Mock
  private ImageService imageService;

  @Mock
  private GroupValidator groupValidator;

  @Mock
  private GroupParticipantValidator groupParticipantValidator;

  @Mock
  private RedisCacheService redisCacheService;

  @Mock
  private GroupWebsocketService groupWebsocketService;

  @InjectMocks
  private GroupCommandService groupCommandService;

  @Test
  @DisplayName("[성공] 모임 생성 성공 테스트")
  void createGroup_Success() {
    // given
    User user = UserFixture.createDefaultUser();
    CustomUserDetails userDetails = new CustomUserDetails(user);

    GroupCreateRequest request = new GroupCreateRequest(
        "테스트 모임",
        "테스트 요약",
        "테스트 설명",
        "IT/개발",
        "서울 강남구",
        10,
        "test-image-url",
        List.of("Spring", "JPA"),
        "테스트 계획"
    );

    Group savedGroup = Group.builder()
        .owner(user)
        .name(request.name())
        .summary(request.summary())
        .description(request.description())
        .plan(request.plan())
        .category(request.category())
        .location(request.location())
        .completedScheduleTotal(0)
        .imageUrl("processed-image.jpg")
        .currentUserCount(0)
        .maxUserCount(request.maxUserCount())
        .status(GroupStatus.ACTIVE)
        .build();

    TestReflectionUtils.setField(savedGroup, "id", 1L);
    List<String> tags = List.of("Spring", "JPA");

    // Mock 설정
    willDoNothing().given(groupValidator).isCategory(request.category());
    given(imageService.uploadGroupImage(request.imageUrl())).willReturn("processed-image.jpg");
    given(groupRepository.saveAndFlush(any(Group.class))).willReturn(savedGroup);
    willDoNothing().given(groupTagService).assignTags(savedGroup, request.tags());
    willDoNothing().given(groupParticipantsCommandService)
        .createToGroupLeader(savedGroup, userDetails.getUser());
    given(groupTagService.getTagNamesByGroupId(savedGroup.getId())).willReturn(tags);
    willDoNothing().given(groupCacheService).deleteGroupListCaches();

    // when
    GroupCreateResponse response = groupCommandService.createGroup(request, userDetails);

    // then
    assertThat(response).isNotNull();
    assertThat(response.groupId()).isEqualTo(1L);
    assertThat(response.name()).isEqualTo("테스트 모임");
    assertThat(response.category()).isEqualTo("IT/개발");
    assertThat(response.summary()).isEqualTo("테스트 요약");
    assertThat(response.description()).isEqualTo("테스트 설명");
    assertThat(response.plan()).isEqualTo("테스트 계획");
    assertThat(response.location()).isEqualTo("서울 강남구");
    assertThat(response.currentUserCount()).isZero();
    assertThat(response.maxUserCount()).isEqualTo(10);
    assertThat(response.imageUrl()).isEqualTo("processed-image.jpg");
    assertThat(response.tags()).isEqualTo(tags);

    // verify
    verify(groupValidator).isCategory(request.category());
    verify(imageService).uploadGroupImage(request.imageUrl());
    verify(groupRepository).saveAndFlush(any(Group.class));
    verify(groupTagService).assignTags(savedGroup, request.tags());
    verify(groupParticipantsCommandService).createToGroupLeader(savedGroup, userDetails.getUser());
    verify(groupTagService).getTagNamesByGroupId(savedGroup.getId());
    verify(groupCacheService).deleteGroupListCaches();
  }

  @Test
  @DisplayName("[성공] 모임 생성 - 태그 없음")
  void createGroup_WithoutTags_Success() {
    // given
    User user = UserFixture.createDefaultUser();
    CustomUserDetails userDetails = new CustomUserDetails(user);

    GroupCreateRequest request = new GroupCreateRequest(
        "태그없는 모임",
        "요약",
        "설명",
        "IT/개발",
        "서울",
        5,
        "image-url",
        null, // 태그 없음
        "계획"
    );

    Group savedGroup = Group.builder()
        .owner(user)
        .name(request.name())
        .summary(request.summary())
        .description(request.description())
        .plan(request.plan())
        .category(request.category())
        .location(request.location())
        .completedScheduleTotal(0)
        .imageUrl("processed-image.jpg")
        .currentUserCount(0)
        .maxUserCount(request.maxUserCount())
        .status(GroupStatus.ACTIVE)
        .build();

    TestReflectionUtils.setField(savedGroup, "id", 1L);

    // Mock 설정
    willDoNothing().given(groupValidator).isCategory(request.category());
    given(imageService.uploadGroupImage(request.imageUrl())).willReturn("processed-image.jpg");
    given(groupRepository.saveAndFlush(any(Group.class))).willReturn(savedGroup);
    willDoNothing().given(groupParticipantsCommandService)
        .createToGroupLeader(savedGroup, userDetails.getUser());
    given(groupTagService.getTagNamesByGroupId(savedGroup.getId())).willReturn(List.of());
    willDoNothing().given(groupCacheService).deleteGroupListCaches();

    // when
    GroupCreateResponse response = groupCommandService.createGroup(request, userDetails);

    // then
    assertThat(response).isNotNull();
    assertThat(response.name()).isEqualTo("태그없는 모임");
    assertThat(response.tags()).isEmpty();

    // verify
    verify(groupValidator).isCategory(request.category());
    verify(groupRepository).saveAndFlush(any(Group.class));
    verify(groupTagService, never()).assignTags(any(), any()); // 태그가 null이므로 호출 안됨
    verify(groupParticipantsCommandService).createToGroupLeader(savedGroup, userDetails.getUser());
    verify(groupTagService).getTagNamesByGroupId(savedGroup.getId());
    verify(groupCacheService).deleteGroupListCaches();
  }

  @Test
  @DisplayName("[성공] 모임 생성 - 빈 태그 리스트")
  void createGroup_WithEmptyTags_Success() {
    // given
    User user = UserFixture.createDefaultUser();
    CustomUserDetails userDetails = new CustomUserDetails(user);

    GroupCreateRequest request = new GroupCreateRequest(
        "빈태그 모임",
        "요약",
        "설명",
        "IT/개발",
        "서울",
        5,
        "image-url",
        List.of(), // 빈 태그 리스트
        "계획"
    );

    Group savedGroup = Group.builder()
        .owner(user)
        .name(request.name())
        .summary(request.summary())
        .description(request.description())
        .plan(request.plan())
        .category(request.category())
        .location(request.location())
        .completedScheduleTotal(0)
        .imageUrl("processed-image.jpg")
        .currentUserCount(0)
        .maxUserCount(request.maxUserCount())
        .status(GroupStatus.ACTIVE)
        .build();

    TestReflectionUtils.setField(savedGroup, "id", 1L);

    // Mock 설정
    willDoNothing().given(groupValidator).isCategory(request.category());
    given(imageService.uploadGroupImage(request.imageUrl())).willReturn("processed-image.jpg");
    given(groupRepository.saveAndFlush(any(Group.class))).willReturn(savedGroup);
    willDoNothing().given(groupParticipantsCommandService)
        .createToGroupLeader(savedGroup, userDetails.getUser());
    given(groupTagService.getTagNamesByGroupId(savedGroup.getId())).willReturn(List.of());
    willDoNothing().given(groupCacheService).deleteGroupListCaches();

    // when
    GroupCreateResponse response = groupCommandService.createGroup(request, userDetails);

    // then
    assertThat(response).isNotNull();
    assertThat(response.name()).isEqualTo("빈태그 모임");
    assertThat(response.tags()).isEmpty();

    // verify - 빈 리스트이므로 assignTags 호출되지 않음
    verify(groupTagService, never()).assignTags(any(), any());
    verify(groupCacheService).deleteGroupListCaches();
  }

  @Test
  @DisplayName("[성공] 모임 삭제")
  void deleteGroup_Success() {
    // given
    Long groupId = 1L;
    Long userId = 1L;

    User owner = UserFixture.createDefaultUser();
    TestReflectionUtils.setField(owner, "id", userId);

    Group group = GroupFixture.createDefaultGroup(owner);
    TestReflectionUtils.setField(group, "id", groupId);

    given(groupValidator.isOwnerForGroupDelete(groupId, userId)).willReturn(group);
    willDoNothing().given(groupCacheService).evictGroupDetailStatic(groupId);
    willDoNothing().given(groupCacheService).deleteGroupListCaches();

    // when
    groupCommandService.deleteGroup(groupId, userId);

    // then
    verify(groupValidator).isOwnerForGroupDelete(groupId, userId);
    verify(groupCacheService).evictGroupDetailStatic(groupId);
    verify(groupCacheService).deleteGroupListCaches();

    // 그룹 상태가 DISBANDED로 변경되었는지 확인 (deleteGroup() 메소드에 의해)
    assertThat(group.getStatus()).isEqualTo(GroupStatus.DISBANDED);
  }

  @Test
  @DisplayName("[성공] 모임 이미지 업데이트")
  void updateGroupImage_Success() {
    // given
    Long groupId = 1L;
    Long userId = 1L;

    User user = UserFixture.createDefaultUser();
    TestReflectionUtils.setField(user, "id", userId);

    Group group = GroupFixture.createDefaultGroup(user);
    TestReflectionUtils.setField(group, "id", groupId);
    TestReflectionUtils.setField(group, "imageUrl", "old-image.jpg");

    UpdateGroupImageRequest request = new UpdateGroupImageRequest("new-image-url");
    LocalDateTime beforeUpdate = group.getUpdatedAt();

    given(groupValidator.findByIdOrThrow(groupId)).willReturn(group);
    willDoNothing().given(groupParticipantValidator).validateIsJoined(groupId, userId);
    given(imageService.updateImage("old-image.jpg", "new-image-url")).willReturn("new-processed-image.jpg");
    willDoNothing().given(groupCacheService).evictGroupDetailStatic(groupId);
    willDoNothing().given(groupCacheService).deleteGroupListCaches();

    // when
    groupCommandService.updateGroupImage(groupId, userId, request);

    // then
    verify(groupValidator).findByIdOrThrow(groupId);
    verify(groupParticipantValidator).validateIsJoined(groupId, userId);
    verify(imageService).updateImage("old-image.jpg", "new-image-url");
    verify(groupCacheService).evictGroupDetailStatic(groupId);
    verify(groupCacheService).deleteGroupListCaches();

    assertThat(group.getImageUrl()).isEqualTo("new-processed-image.jpg");
    assertThat(group.getUpdatedAt()).isAfter(beforeUpdate);
  }

  @Test
  @DisplayName("[성공] 새 챗봇 세션 생성")
  void createNewChatbotSession_Success() {
    // given
    User user = UserFixture.createDefaultUser();
    TestReflectionUtils.setField(user, "id", 1L);
    CustomUserDetails userDetails = new CustomUserDetails(user);

    willDoNothing().given(redisCacheService).set(anyString(), any(Map.class), eq(Duration.ofMinutes(30)));

    // when
    String sessionId = groupCommandService.createNewChatbotSession(userDetails);

    // then
    assertThat(sessionId).isNotNull();
    assertThat(sessionId).hasSize(36); // UUID 길이 확인

    verify(redisCacheService).set(anyString(), any(Map.class), eq(Duration.ofMinutes(30)));
  }
}
