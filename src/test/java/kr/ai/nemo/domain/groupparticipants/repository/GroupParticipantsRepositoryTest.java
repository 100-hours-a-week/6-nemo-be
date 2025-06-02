package kr.ai.nemo.domain.groupparticipants.repository;

import java.time.LocalDateTime;
import java.util.List;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
import kr.ai.nemo.global.testUtil.TestReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("GroupParticipantsRepository 테스트")
class GroupParticipantsRepositoryTest {

    @Autowired
    private GroupParticipantsRepository groupParticipantsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    Group savedGroup;
    User savedUser;
    User savedUser1;
    GroupParticipants participant;
    @BeforeEach
    void setUp() {
        User user = UserFixture.createDefaultUser();
        savedUser = userRepository.save(user);
        TestReflectionUtils.setId(savedUser, "id", 1L);

        User user1 = UserFixture.createUser("testUser", "test11@example.com", "kakao", "12341234");
        savedUser1 = userRepository.save(user1);
        TestReflectionUtils.setId(savedUser1, "id", 2L);

        Group group = GroupFixture.createDefaultGroup(savedUser);
        savedGroup = groupRepository.save(group);
        TestReflectionUtils.setId(savedGroup, "id", 1L);

        participant = GroupParticipants.builder()
            .user(savedUser1)
            .group(savedGroup)
            .role(Role.MEMBER)
            .status(Status.JOINED)
            .appliedAt(LocalDateTime.now())
            .build();

        participant = groupParticipantsRepository.save(participant);
    }

    @Test
    @DisplayName("[성공] 모임원 저장 테스트")
    void save_Success() {
        // when
        GroupParticipants savedParticipant = groupParticipantsRepository.save(participant);
        
        // then
        assertThat(savedParticipant.getUser()).isEqualTo(savedUser1);
        assertThat(savedParticipant.getId()).isNotNull();
        assertThat(savedParticipant.getRole()).isEqualTo(Role.MEMBER);
        assertThat(savedParticipant.getStatus()).isEqualTo(Status.JOINED);
    }

    @Test
    @DisplayName("[성공] 이미 모임에 신청했는지 검증 테스트")
    void existsByGroupIdAndUserIdAndStatusIn_success() {
        // when
        boolean exists = groupParticipantsRepository.existsByGroupIdAndUserIdAndStatusIn(savedGroup.getId(), savedUser1.getId(), List.of(Status.JOINED));

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("[성공] 모임원 list 조회 테스트")
    void findByGroupIdAndStatusIn_success() {
        // when
        List<GroupParticipants> participants = groupParticipantsRepository.findByGroupIdAndStatus(savedGroup.getId(), Status.JOINED);

        // then
        assertThat(participants).hasSize(1);
        assertThat(participants.getFirst().getUser()).isEqualTo(savedUser1);
    }

    @Test
    @DisplayName("[성공] 모임원인지 검증 테스트")
    void existsByGroupIdAndUserIdAndStatus_success() {
        // when
        boolean exists = groupParticipantsRepository.existsByGroupIdAndUserIdAndStatus(savedGroup.getId(), savedUser1.getId(), Status.JOINED);

        // then
        assertThat(exists).isTrue();
    }
}
