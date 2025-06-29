package kr.ai.nemo.domain.group.repository;

import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.GroupTag;
import kr.ai.nemo.domain.group.domain.Tag;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@DisplayName("GroupTagRepository 테스트")
class GroupTagRepositoryTest {

    @Autowired
    private GroupTagRepository groupTagRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    private Group savedGroup;
    private Tag savedTag;

    @BeforeEach
    void setUp() {
        // 공통 테스트 데이터 준비
        User user = UserFixture.createDefaultUser();
        User savedUser = userRepository.save(user);
        userRepository.flush();

        Group group = GroupFixture.createDefaultGroup(savedUser);
        savedGroup = groupRepository.save(group);
        groupRepository.flush();

        Tag tag = TagFixture.createDefaultTag();
        savedTag = tagRepository.save(tag);
        tagRepository.flush();
    }

    @Test
    @DisplayName("[성공] 모임 상세 조회 테스트")
    void findByGroup_Success() {
        // given
        GroupTag groupTag = new GroupTag(savedGroup, savedTag);
        groupTagRepository.save(groupTag);
        groupTagRepository.flush();

        // when
        List<GroupTag> result = groupTagRepository.findByGroupId(savedGroup.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGroup().getId()).isEqualTo(savedGroup.getId());
        assertThat(result.get(0).getTag().getId()).isEqualTo(savedTag.getId());
    }

    @Test
    @DisplayName("[성공] 그룹 태그 저장 테스트")
    void save_Success() {
        // given
        GroupTag groupTag = new GroupTag(savedGroup, savedTag);

        // when
        GroupTag savedGroupTag = groupTagRepository.save(groupTag);
        groupTagRepository.flush();

        // then
        assertThat(savedGroupTag.getId()).isNotNull();
        assertThat(savedGroupTag.getGroup().getId()).isEqualTo(savedGroup.getId());
        assertThat(savedGroupTag.getTag().getId()).isEqualTo(savedTag.getId());
    }
}
