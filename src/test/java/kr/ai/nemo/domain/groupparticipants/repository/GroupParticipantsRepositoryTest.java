package kr.ai.nemo.domain.groupparticipants.repository;

import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.repository.UserRepository;
import kr.ai.nemo.global.fixture.user.UserFixture;
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

    @Test
    @DisplayName("그룹 참가자 저장 테스트")
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
        Group savedGroup = groupRepository.save(group);
        
        GroupParticipants participant = GroupParticipants.builder()
                .user(savedUser)
                .group(savedGroup)
                .role(Role.MEMBER)
                .status(Status.JOINED)
                .build();
        
        // when
        GroupParticipants savedParticipant = groupParticipantsRepository.save(participant);
        
        // then
        assertThat(savedParticipant.getId()).isNotNull();
        assertThat(savedParticipant.getRole()).isEqualTo(Role.MEMBER);
        assertThat(savedParticipant.getStatus()).isEqualTo(Status.JOINED);
    }
}
