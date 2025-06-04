package kr.ai.nemo.domain.group.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.util.List;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.dto.request.GroupSearchRequest;
import kr.ai.nemo.domain.group.dto.response.GroupDetailResponse;
import kr.ai.nemo.domain.group.dto.response.GroupListResponse;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupQueryService 테스트")
class GroupQueryServiceTest {

  @Mock
  private GroupRepository groupRepository;

  @Mock
  private GroupValidator groupValidator;

  @Mock
  private GroupTagService groupTagService;

  @InjectMocks
  private GroupQueryService groupQueryService;

  private Page<Group> groupPage;
  private GroupSearchRequest request;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    // given
    request = new GroupSearchRequest();
    pageable = Pageable.ofSize(10);

    Group group = mock(Group.class);
    groupPage = new PageImpl<>(List.of(group));

  }
  @Test
  @DisplayName("[성공] 카테고리별 모임 조회")
  void getGroups_withCategory_Success() {
    // given
    request.setCategory("IT/개발");
    given(groupRepository.findByCategoryAndStatusNot("IT/개발", GroupStatus.DISBANDED, pageable))
        .willReturn(groupPage);

    // when
    GroupListResponse response = groupQueryService.getGroups(request, pageable);

    // then
    assertThat(response).isNotNull();
    then(groupRepository).should(times(1)).findByCategoryAndStatusNot(request.getCategory(), GroupStatus.DISBANDED, pageable);
  }

  @Test
  @DisplayName("[성공] 키워드 검색 모임 조회")
  void getGroups_withKeyword_Success() {
    //given
    request.setKeyword("IT/개발");
    given(groupRepository.searchWithKeywordOnly("IT/개발", pageable))
        .willReturn(groupPage);

    // when
    GroupListResponse response = groupQueryService.getGroups(request, pageable);

    // then
    assertThat(response).isNotNull();
    then(groupRepository).should(times(1)).searchWithKeywordOnly(request.getKeyword(), pageable);
  }

  @Test
  @DisplayName("[성공] 전체 모임 조회")
  void getGroups_all_Success() {
    //given
    given(groupRepository.findByStatusNot(GroupStatus.DISBANDED, pageable))
        .willReturn(groupPage);

    // when
    GroupListResponse response = groupQueryService.getGroups(request, pageable);

    // then
    assertThat(response).isNotNull();
    then(groupRepository).should(times(1)).findByStatusNot(GroupStatus.DISBANDED, pageable);
  }

  @Test
  @DisplayName("[성공] 모임 상세 조회")
  void getGroup_detail_Success() {
    // given
    User user = UserFixture.createDefaultUser();
    CustomUserDetails customUserDetails = new CustomUserDetails(user);
    Group group = GroupFixture.createDefaultGroup(user);
    Long groupId = 1L;
    ReflectionTestUtils.setField(group, "id", groupId);

    given(groupValidator.findByIdOrThrow(groupId)).willReturn(group);

    List<String> tags = List.of("tag1", "tag2");
    given(groupTagService.getTagNamesByGroupId(groupId)).willReturn(tags);

    // when
    GroupDetailResponse response = groupQueryService.detailGroup(groupId, customUserDetails);

    // then
    then(groupValidator).should(times(1)).findByIdOrThrow(groupId);
    then(groupTagService).should(times(1)).getTagNamesByGroupId(groupId);
    assertThat(response).isNotNull();
    assertThat(response.tags()).isEqualTo(tags);
  }
}
