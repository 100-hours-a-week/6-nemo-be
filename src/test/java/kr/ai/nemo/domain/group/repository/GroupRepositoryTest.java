package kr.ai.nemo.domain.group.repository;

import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.GroupTag;
import kr.ai.nemo.domain.group.domain.Tag;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.group.TagFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("GroupRepository 테스트")
class GroupRepositoryTest {

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TagRepository tagRepository;

  @Autowired
  private GroupTagRepository groupTagRepository;

  private Group group;
  private User savedUser;
  private Group savedGroup;
  private Tag savedTag;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    // 공통 테스트 데이터 준비
    User user = UserFixture.createDefaultUser();
    savedUser = userRepository.saveAndFlush(user);

    group = GroupFixture.createDefaultGroup(savedUser);
    savedGroup = groupRepository.saveAndFlush(group);

    Tag tag = TagFixture.createDefaultTag();
    savedTag = tagRepository.save(tag);

    groupTagRepository.save(new GroupTag(savedGroup, savedTag));

    pageable = PageRequest.of(0, 10);
  }

  @Test
  @DisplayName("[성공] 모임 저장 테스트")
  void save_Success() {
    // then
    assertThat(savedGroup.getId()).isNotNull();
    assertThat(savedGroup.getName()).isEqualTo(group.getName());
    assertThat(savedGroup.getOwner().getId()).isEqualTo(savedUser.getId());
  }

  @Test
  @DisplayName("[성공] 그룹명으로 검색")
  void searchByGroupName_ShouldReturnGroup_WhenNameMatches() {
    // when
    Page<Group> result = groupRepository.searchWithKeywordOnly(group.getName(), pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(1L);
    assertThat(result.getContent())
        .hasSize(1)
        .first()
        .extracting(Group::getName)
        .isEqualTo(group.getName());
  }

  @Test
  @DisplayName("[성공] 그룹 요약으로 검색")
  void searchByGroupSummary_ShouldReturnGroup_WhenSummaryMatches() {
    // when
    Page<Group> result = groupRepository.searchWithKeywordOnly(group.getSummary(), pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(1L);
    assertThat(result.getContent())
        .hasSize(1)
        .first()
        .extracting(Group::getSummary)
        .isEqualTo(group.getSummary());
  }

  @Test
  @DisplayName("[성공] 태그명으로 검색")
  void searchByTagName_ShouldReturnGroup_WhenTagMatches() {
    // when
    Page<Group> result = groupRepository.searchWithKeywordOnly(savedTag.getName(), pageable);

    // then
    assertThat(result.getTotalElements()).isEqualTo(1L);
    assertThat(result.getContent()).hasSize(1);

    Group foundGroup = result.getContent().getFirst();
    assertThat(foundGroup.getGroupTags())
        .extracting(groupTag -> groupTag.getTag().getName())
        .contains(savedTag.getName());
  }


  @Test
  @DisplayName("[성공] 카테고리별 모임 list 조회")
  void findByCategory_Success() {
    // when
    Page<Group> result = groupRepository.findByCategoryAndStatusNot(group.getCategory(), GroupStatus.DISBANDED, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isNotEmpty();
    assertThat(result.getContent().getFirst().getName()).isEqualTo(group.getName());
  }

  @Test
  @DisplayName("[성공] 전체 모임 list 조회")
  void findByStatusNot_Success() {
    // when
    Page<Group> result = groupRepository.findByStatusNot(GroupStatus.DISBANDED, pageable);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent().getFirst().getName()).isEqualTo(group.getName());
  }
}
