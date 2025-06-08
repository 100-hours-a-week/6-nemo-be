package kr.ai.nemo.domain.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
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
  private ImageService imageService;

  @Mock
  private GroupValidator groupValidator;

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

    List<String> tags = List.of("Spring", "JPA");

    willDoNothing().given(groupValidator).isCategory(request.category());
    given(imageService.uploadGroupImage(request.imageUrl())).willReturn("processed-image.jpg");
    given(groupRepository.save(any(Group.class))).willAnswer(invocation -> {
      Group groupToSave = invocation.getArgument(0);
      TestReflectionUtils.setField(groupToSave, "id", 1L);
      return groupToSave;
    });

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
    assertThat(response.imageUrl()).isEqualTo("processed-image.jpg"); // ImageService Mock 결과
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

    User user = mock(User.class);
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
    User user = mock(User.class);

    Group group = GroupFixture.createDefaultGroup(user);
    String oldImage = group.getImageUrl();

    UpdateGroupImageRequest request = new UpdateGroupImageRequest("newGroupImage");

    given(groupValidator.isOwnerForGroupUpdate(groupId, userId)).willReturn(group);
    given(imageService.updateImage(group.getImageUrl(), request.imageUrl())).willReturn("newGroupImage.jpg");

    // when
    groupCommandService.updateGroupImage(groupId, userId, request);

    // then
    assertThat(group.getImageUrl()).isEqualTo("newGroupImage.jpg");
    verify(imageService).updateImage(oldImage, request.imageUrl());
  }
}
