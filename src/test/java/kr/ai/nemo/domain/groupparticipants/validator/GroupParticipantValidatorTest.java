package kr.ai.nemo.domain.groupparticipants.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.exception.GroupParticipantException;
import kr.ai.nemo.domain.groupparticipants.repository.GroupParticipantsRepository;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.unit.global.fixture.group.GroupFixture;
import kr.ai.nemo.unit.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupParticipantValidator 테스트")
class GroupParticipantValidatorTest {

    @Mock
    private GroupParticipantsRepository repository;

    @InjectMocks
    private GroupParticipantValidator groupParticipantValidator;

    @Test
    @DisplayName("[성공] 참여하지 않은 멤버 검증")
    void validateJoinedParticipant_NotJoined_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        GroupParticipants participant = GroupParticipants.builder()
                .user(user)
                .group(group)
                .role(Role.MEMBER)
                .status(Status.WITHDRAWN) // 참여하지 않은 상태
                .appliedAt(LocalDateTime.now())
                .build();

        // when & then
        groupParticipantValidator.validateJoinedParticipant(participant); // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("[실패] 이미 참여한 멤버 검증")
    void validateJoinedParticipant_AlreadyJoined_ThrowException() {
        // given
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        GroupParticipants participant = GroupParticipants.builder()
                .user(user)
                .group(group)
                .role(Role.MEMBER)
                .status(Status.JOINED) // 이미 참여한 상태
                .appliedAt(LocalDateTime.now())
                .build();

        // when & then
        assertThatThrownBy(() -> groupParticipantValidator.validateJoinedParticipant(participant))
                .isInstanceOf(GroupException.class);
    }

    @Test
    @DisplayName("[성공] 참여한 멤버 여부 확인 - true")
    void validateIsJoinedMember_Joined_ReturnTrue() {
        // given
        Long groupId = 1L;
        Long userId = 1L;

        given(repository.existsByGroupIdAndUserIdAndStatus(groupId, userId, Status.JOINED))
                .willReturn(true);

        // when
        boolean result = groupParticipantValidator.validateIsJoinedMember(groupId, userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("[성공] 참여한 멤버 여부 확인 - false")
    void validateIsJoinedMember_NotJoined_ReturnFalse() {
        // given
        Long groupId = 1L;
        Long userId = 1L;

        given(repository.existsByGroupIdAndUserIdAndStatus(groupId, userId, Status.JOINED))
                .willReturn(false);

        // when
        boolean result = groupParticipantValidator.validateIsJoinedMember(groupId, userId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("[성공] 사용자 역할 확인 - 그룹 리더")
    void checkUserRole_GroupOwner_ReturnLeader() {
        // given
        User owner = User.builder()
            .id(1L)
            .nickname("owner")
            .email("owner@example.com")
            .provider("kakao")
            .providerId("123")
            .build();

        Group group = GroupFixture.createGroupWithId(1L, owner, "테스트 그룹");

        GroupParticipants participant = GroupParticipants.builder()
            .user(owner)
            .group(group)
            .role(Role.LEADER)
            .status(Status.JOINED)
            .build();

        CustomUserDetails userDetails = new CustomUserDetails(owner);

        given(repository.findByGroupIdAndUserId(1L, 1L))
            .willReturn(Optional.of(participant));

        // when
        Role result = groupParticipantValidator.checkUserRole(userDetails, group.getId());

        // then
        assertThat(result).isEqualTo(Role.LEADER);
    }

    @Test
    @DisplayName("[성공] 사용자 역할 확인 - 그룹 멤버")
    void checkUserRole_GroupMember_ReturnMember() {
        // given
        User owner = User.builder()
            .id(1L)
            .nickname("owner")
            .email("owner@example.com")
            .provider("kakao")
            .providerId("123")
            .build();

        Group group = GroupFixture.createGroupWithId(1L, owner, "테스트 그룹");

        GroupParticipants participant = GroupParticipants.builder()
            .user(owner)
            .group(group)
            .role(Role.MEMBER)
            .status(Status.JOINED)
            .build();

        CustomUserDetails userDetails = new CustomUserDetails(owner);

        given(repository.findByGroupIdAndUserId(1L, 1L))
            .willReturn(Optional.of(participant));

        // when
        Role result = groupParticipantValidator.checkUserRole(userDetails, group.getId());

        // then
        assertThat(result).isEqualTo(Role.MEMBER);
    }

    @Test
    @DisplayName("[성공] 사용자 역할 확인 - 비멤버")
    void checkUserRole_NonMember_ReturnNonMember() {
        // given
        User owner = User.builder()
            .id(1L)
            .nickname("owner")
            .email("owner@example.com")
            .provider("kakao")
            .providerId("123")
            .build();

        Group group = GroupFixture.createGroupWithId(1L, owner, "테스트 그룹");
        CustomUserDetails userDetails = new CustomUserDetails(owner);

        // when
        Role result = groupParticipantValidator.checkUserRole(userDetails, group.getId());

        // then
        assertThat(result).isEqualTo(Role.NON_MEMBER);
    }


    @Test
    @DisplayName("[성공] 사용자 역할 확인 - 게스트 (미로그인)")
    void checkUserRole_Guest_ReturnGuest() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = GroupFixture.createGroupWithId(1L, owner, "테스트 그룹");

        // when
        Role result = groupParticipantValidator.checkUserRole(null, group.getId());

        // then
        assertThat(result).isEqualTo(Role.GUEST);
    }

    @Test
    @DisplayName("[성공] 참가자 조회 - 정상 참가자")
    void getParticipant_ValidParticipant_Success() {
        // given
        Long groupId = 1L;
        Long userId = 1L;
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        
        GroupParticipants participant = GroupParticipants.builder()
                .user(user)
                .group(group)
                .role(Role.MEMBER)
                .status(Status.JOINED)
                .appliedAt(LocalDateTime.now())
                .build();

        given(repository.findByGroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(participant));

        // when
        GroupParticipants result = groupParticipantValidator.getParticipant(groupId, userId);

        // then
        assertThat(result).isEqualTo(participant);
    }

    @Test
    @DisplayName("[실패] 참가자 조회 - 존재하지 않음")
    void getParticipant_NotFound_ThrowException() {
        // given
        Long groupId = 1L;
        Long userId = 999L;

        given(repository.findByGroupIdAndUserId(groupId, userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupParticipantValidator.getParticipant(groupId, userId))
                .isInstanceOf(GroupParticipantException.class);
    }

    @Test
    @DisplayName("[실패] 참가자 조회 - 추방된 멤버")
    void getParticipant_KickedMember_ThrowException() {
        // given
        Long groupId = 1L;
        Long userId = 1L;
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        
        GroupParticipants kickedParticipant = GroupParticipants.builder()
                .user(user)
                .group(group)
                .role(Role.MEMBER)
                .status(Status.KICKED)
                .appliedAt(LocalDateTime.now())
                .build();

        given(repository.findByGroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(kickedParticipant));

        // when & then
        assertThatThrownBy(() -> groupParticipantValidator.getParticipant(groupId, userId))
                .isInstanceOf(GroupParticipantException.class);
    }

    @Test
    @DisplayName("[실패] 참가자 조회 - 탈퇴한 멤버")
    void getParticipant_WithdrawnMember_ThrowException() {
        // given
        Long groupId = 1L;
        Long userId = 1L;
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        
        GroupParticipants withdrawnParticipant = GroupParticipants.builder()
                .user(user)
                .group(group)
                .role(Role.MEMBER)
                .status(Status.WITHDRAWN)
                .appliedAt(LocalDateTime.now())
                .build();

        given(repository.findByGroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(withdrawnParticipant));

        // when & then
        assertThatThrownBy(() -> groupParticipantValidator.getParticipant(groupId, userId))
                .isInstanceOf(GroupParticipantException.class);
    }

    @Test
    @DisplayName("[성공] 소유자 체크 - 일반 멤버")
    void checkOwner_RegularMember_Success() {
        // given
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        
        GroupParticipants participant = GroupParticipants.builder()
                .user(user)
                .group(group)
                .role(Role.MEMBER) // 일반 멤버
                .status(Status.JOINED)
                .appliedAt(LocalDateTime.now())
                .build();

        // when & then
        groupParticipantValidator.checkOwner(participant); // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("[실패] 소유자 체크 - 리더는 제거할 수 없음")
    void checkOwner_Leader_ThrowException() {
        // given
        User user = UserFixture.createDefaultUser();
        Group group = GroupFixture.createDefaultGroup(user);
        
        GroupParticipants leaderParticipant = GroupParticipants.builder()
                .user(user)
                .group(group)
                .role(Role.LEADER) // 리더
                .status(Status.JOINED)
                .appliedAt(LocalDateTime.now())
                .build();

        // when & then
        assertThatThrownBy(() -> groupParticipantValidator.checkOwner(leaderParticipant))
                .isInstanceOf(GroupParticipantException.class);
    }
}
