package kr.ai.nemo.domain.group.repository;

import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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

    @Test
    @DisplayName("그룹 저장 테스트")
    void save_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        User savedUser = userRepository.save(user);
        
        Group group = Group.builder()
                .name("테스트 그룹")
                .summary("테스트 그룹 요약")
                .description("테스트 그룹 설명")
                .category("스터디")
                .location("서울시 강남구")
                .maxUserCount(10)
                .currentUserCount(1)
                .completedScheduleTotal(0)
                .status(GroupStatus.ACTIVE)
                .owner(savedUser)
                .build();
        
        // when
        Group savedGroup = groupRepository.save(group);
        
        // then
        assertThat(savedGroup.getId()).isNotNull();
        assertThat(savedGroup.getName()).isEqualTo("테스트 그룹");
        assertThat(savedGroup.getOwner().getId()).isEqualTo(savedUser.getId());
    }
}
