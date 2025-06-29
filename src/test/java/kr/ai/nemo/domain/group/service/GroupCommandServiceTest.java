package kr.ai.nemo.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.dto.request.GroupCreateRequest;
import kr.ai.nemo.domain.group.dto.request.UpdateGroupImageRequest;
import kr.ai.nemo.domain.group.dto.response.GroupCreateResponse;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsCommandService;
import kr.ai.nemo.domain.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
import kr.ai.nemo.global.redis.RedisCacheService;
import kr.ai.nemo.global.testUtil.TestReflectionUtils;
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
  private GroupTagService groupTagService;

  @Mock
  private GroupParticipantsCommandService groupParticipantsCommandService;

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

  @InjectMocks
  private GroupCommandService groupCommandService;

  @Test
  @DisplayName("[성공] 그룹 생성 성공 테스트")
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

    willDoNothing().given(groupValidator).isCategory(request.category());
    given(imageService.uploadGroupImage(request.imageUrl())).willReturn("processed-image.jpg");
    given(groupRepository.save(any(Group.class))).willReturn(savedGroup);
    willDoNothing().given(groupTagService).assignTags(any(Group.class), anyList());
    willDoNothing().given(groupParticipantsCommandService)
        .applyToGroup(anyLong(), any(CustomUserDetails.class), any(Role.class), any(Status.class));
    given(groupTagService.getTagNamesByGroupId(any(Long.class))).willReturn(tags);

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
    then(groupValidator).should().isCategory(request.category());
    then(imageService).should().uploadGroupImage(request.imageUrl());
    then(groupRepository).should().save(any(Group.class));
    then(groupTagService).should().assignTags(any(Group.class), anyList());
    then(groupParticipantsCommandService).should()
        .applyToGroup(anyLong(), any(CustomUserDetails.class), any(Role.class), any(Status.class));
    then(groupTagService).should().getTagNamesByGroupId(any(Long.class));
  }

  @Test
  @DisplayName("[성공] 모임 해체 테스트")
  void deleteGroup_Success() {
    // given
    Long userId = 1L;
    Long groupId = 1L;

    User user = UserFixture.createDefaultUser();
    Group group = GroupFixture.createDefaultGroup(user);

    given(groupValidator.isOwnerForGroupDelete(groupId, userId)).willReturn(group);

    // when
    groupCommandService.deleteGroup(groupId, userId);

    // then
    assertThat(group.getStatus()).isEqualTo(GroupStatus.DISBANDED);
    assertThat(group.getDeletedAt()).isNotNull();
    assertThat(group.getGroupTags()).isEmpty();
  }

  @Test
  @DisplayName("[성공] 모임 대표 사진 수정 테스트")
  void updateGroupImage_Success() {
    // given
    Long userId = 1L;
    Long groupId = 1L;
    User user = UserFixture.createDefaultUser();

    Group group = GroupFixture.createDefaultGroup(user);
    String oldImage = group.getImageUrl();
    LocalDateTime beforeUpdate = group.getUpdatedAt();

    UpdateGroupImageRequest request = new UpdateGroupImageRequest("newGroupImage");

    given(groupValidator.findByIdOrThrow(groupId)).willReturn(group);
    willDoNothing().given(groupParticipantValidator).validateIsJoined(groupId, userId);
    given(imageService.updateImage(oldImage, request.imageUrl())).willReturn("newGroupImage.jpg");

    // when
    groupCommandService.updateGroupImage(groupId, userId, request);

    // then
    assertThat(group.getImageUrl()).isEqualTo("newGroupImage.jpg");
    assertThat(group.getUpdatedAt()).isAfter(beforeUpdate);
    verify(imageService).updateImage(oldImage, request.imageUrl());
    verify(groupParticipantValidator).validateIsJoined(groupId, userId);
  }

  @Test
  @DisplayName("[성공] 그룹 생성 - 태그 없음")
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

    willDoNothing().given(groupValidator).isCategory(request.category());
    given(imageService.uploadGroupImage(request.imageUrl())).willReturn("processed-image.jpg");
    given(groupRepository.save(any(Group.class))).willReturn(savedGroup);
    willDoNothing().given(groupParticipantsCommandService)
        .applyToGroup(anyLong(), any(CustomUserDetails.class), any(Role.class), any(Status.class));
    given(groupTagService.getTagNamesByGroupId(anyLong())).willReturn(Arrays.asList());

    // when
    GroupCreateResponse response = groupCommandService.createGroup(request, userDetails);

    // then
    assertThat(response).isNotNull();
    assertThat(response.name()).isEqualTo("태그없는 모임");
    assertThat(response.tags()).isEmpty();

    verify(groupValidator).isCategory(request.category());
    verify(groupRepository).save(any(Group.class));
    // 태그가 없으므로 assignTags는 호출되지 않음
  }

  @Test
  @DisplayName("[성공] 챗봇 세션 생성")
  void createNewChatbotSession_Success() {
    // given
    User user = UserFixture.createDefaultUser();
    CustomUserDetails userDetails = new CustomUserDetails(user);

    willDoNothing().given(redisCacheService).set(anyString(), any(), any());

    // when
    String sessionId = groupCommandService.createNewChatbotSession(userDetails);

    // then
    assertThat(sessionId).isNotNull();
    assertThat(sessionId).isNotEmpty();
    
    verify(redisCacheService).set(anyString(), any(), any());
  }
}
