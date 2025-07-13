package kr.ai.nemo.domain.groupparticipants.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.group.GroupFixture;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GroupParticipants 도메인 테스트")
class GroupParticipantsTest {

    @Test
    @DisplayName("[성공] GroupParticipants 생성")
    void createGroupParticipants_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        Role role = Role.MEMBER;
        Status status = Status.JOINED;
        LocalDateTime appliedAt = LocalDateTime.now();

        // when
        GroupParticipants participants = GroupParticipants.builder()
                .user(user)
                .group(group)
                .role(role)
                .status(status)
                .appliedAt(appliedAt)
                .build();

        // then
        assertThat(participants.getUser()).isEqualTo(user);
        assertThat(participants.getGroup()).isEqualTo(group);
        assertThat(participants.getRole()).isEqualTo(role);
        assertThat(participants.getStatus()).isEqualTo(status);
        assertThat(participants.getAppliedAt()).isEqualTo(appliedAt);
    }

    @Test
    @DisplayName("[성공] 재가입")
    void rejoin_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        GroupParticipants participants = GroupParticipants.builder()
                .user(user)
                .group(group)
                .role(Role.MEMBER)
                .status(Status.WITHDRAWN)
                .appliedAt(LocalDateTime.now().minusDays(1))
                .deletedAt(LocalDateTime.now().minusHours(1))
                .build();

        // when
        participants.rejoin();

        // then
        assertThat(participants.getStatus()).isEqualTo(Status.JOINED);
        assertThat(participants.getDeletedAt()).isNull();
        assertThat(participants.getAppliedAt()).isNotNull();
    }

    @Test
    @DisplayName("[성공] 상태 설정")
    void setStatus_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        GroupParticipants participants = GroupParticipants.builder()
                .user(user)
                .group(group)
                .role(Role.MEMBER)
                .status(Status.JOINED)
                .appliedAt(LocalDateTime.now())
                .build();

        // when
        participants.setStatus(Status.KICKED);

        // then
        assertThat(participants.getStatus()).isEqualTo(Status.KICKED);
        assertThat(participants.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("[성공] 탈퇴 상태로 설정")
    void setStatus_Withdrawn_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        GroupParticipants participants = GroupParticipants.builder()
                .user(user)
                .group(group)
                .role(Role.MEMBER)
                .status(Status.JOINED)
                .appliedAt(LocalDateTime.now())
                .build();

        // when
        participants.setStatus(Status.WITHDRAWN);

        // then
        assertThat(participants.getStatus()).isEqualTo(Status.WITHDRAWN);
        assertThat(participants.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("[성공] 일반 참여자가 아닌 상태로 설정")
    void setStatus_NonJoined_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        GroupParticipants participants = GroupParticipants.builder()
                .user(user)
                .group(group)
                .role(Role.MEMBER)
                .status(Status.JOINED)
                .appliedAt(LocalDateTime.now())
                .build();

        // when
        participants.setStatus(Status.JOINED);

        // then
        assertThat(participants.getStatus()).isEqualTo(Status.JOINED);
    }

    @Test
    @DisplayName("[성공] 생성자를 통한 객체 생성")
    void createWithConstructor_Success() {
        // given
        Long id = 1L;
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        Role role = Role.LEADER;
        Status status = Status.JOINED;
        LocalDateTime appliedAt = LocalDateTime.now();
        LocalDateTime joinedAt = LocalDateTime.now();
        LocalDateTime deletedAt = null;

        // when
        GroupParticipants participants = new GroupParticipants(
                id, user, group, role, status, appliedAt, joinedAt, deletedAt
        );

        // then
        assertThat(participants.getId()).isEqualTo(id);
        assertThat(participants.getUser()).isEqualTo(user);
        assertThat(participants.getGroup()).isEqualTo(group);
        assertThat(participants.getRole()).isEqualTo(role);
        assertThat(participants.getStatus()).isEqualTo(status);
        assertThat(participants.getAppliedAt()).isEqualTo(appliedAt);
        assertThat(participants.getJoinedAt()).isEqualTo(joinedAt);
        assertThat(participants.getDeletedAt()).isEqualTo(deletedAt);
    }
}
