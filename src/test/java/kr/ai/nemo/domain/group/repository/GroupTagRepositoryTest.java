package kr.ai.nemo.domain.group.repository;

import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.GroupTag;
import kr.ai.nemo.domain.group.domain.Tag;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.group.TagFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("GroupTagRepository 테스트")
class GroupTagRepositoryTest {

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private TagRepository tagRepository;

  @Autowired
  private GroupTagRepository groupTagRepository;
  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("[성공] 모임 상세 조회 테스트")
  void findByGroupId_Success() {
    // given
    User user = userRepository.save(UserFixture.createDefaultUser());
    Group group = groupRepository.save(GroupFixture.createDefaultGroup(user));
    Tag tag = tagRepository.save(TagFixture.createDefaultTag());

    GroupTag groupTag = GroupTag.builder()
        .group(group)
        .tag(tag)
        .build();
    groupTagRepository.save(groupTag);

    // when
    List<GroupTag> result = groupTagRepository.findByGroupId(group.getId());

    // then
    assertThat(result).hasSize(result.size());
    assertThat(result.getFirst().getTag().getName()).isEqualTo("testTag");
  }

  @Test
  @DisplayName("[실패] 모임 상세 조회 테스트")
  void findByGroupId_Fail() {
    // given
    Long invalidGroupId = 9999L; // DB에 없는 id

    // when
    List<GroupTag> result = groupTagRepository.findByGroupId(invalidGroupId);

    // then
    assertThat(result).isEmpty();
  }
}
