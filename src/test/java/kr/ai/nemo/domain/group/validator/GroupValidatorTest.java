package kr.ai.nemo.domain.group.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GroupValidator 테스트")
class GroupValidatorTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupValidator groupValidator;

    @Test
    @DisplayName("[성공] 그룹 ID로 그룹 조회")
    void findByIdOrThrow_Success() {
        // given
        Long groupId = 1L;
        Group mockGroup = createMockGroup(groupId, GroupStatus.ACTIVE);
        
        given(groupRepository.findByIdGroupId(groupId)).willReturn(Optional.of(mockGroup));

        // when
        Group result = groupValidator.findByIdOrThrow(groupId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(groupId);
        assertThat(result.getStatus()).isEqualTo(GroupStatus.ACTIVE);
    }

    @Test
    @DisplayName("[실패] 그룹 ID로 그룹 조회 - 그룹이 존재하지 않음")
    void findByIdOrThrow_GroupNotFound_ThrowException() {
        // given
        Long groupId = 999L;
        given(groupRepository.findByIdGroupId(groupId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupValidator.findByIdOrThrow(groupId))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("errorCode", GroupErrorCode.GROUP_NOT_FOUND);
    }

    @Test
    @DisplayName("[실패] 그룹 ID로 그룹 조회 - 해산된 그룹")
    void findByIdOrThrow_DisbandedGroup_ThrowException() {
        // given
        Long groupId = 1L;
        Group disbandedGroup = createMockGroup(groupId, GroupStatus.DISBANDED);
        
        given(groupRepository.findByIdGroupId(groupId)).willReturn(Optional.of(disbandedGroup));

        // when & then
        assertThatThrownBy(() -> groupValidator.findByIdOrThrow(groupId))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("errorCode", GroupErrorCode.GROUP_DISBANDED);
    }

    @Test
    @DisplayName("[성공] 유효한 카테고리 검증")
    void isCategory_ValidCategory_Success() {
        // given
        String validCategory = "IT/개발";

        // when & then (예외가 발생하지 않으면 성공)
        groupValidator.isCategory(validCategory);
        // verify 제거 - groupValidator는 @InjectMocks로 실제 객체이므로 mock이 아님
    }

    @Test
    @DisplayName("[실패] 유효하지 않은 카테고리 검증")
    void isCategory_InvalidCategory_ThrowException() {
        // given
        String invalidCategory = "잘못된카테고리";

        // when & then
        assertThatThrownBy(() -> groupValidator.isCategory(invalidCategory))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("errorCode", GroupErrorCode.INVALID_CATEGORY);
    }

    @Test
    @DisplayName("[성공] 그룹이 가득 차지 않음 검증")
    void validateGroupIsNotFull_NotFull_Success() {
        // given
        Group group = createMockGroupWithCapacity(10, 5); // 최대 10명, 현재 5명

        // when & then (예외가 발생하지 않으면 성공)
        groupValidator.validateGroupIsNotFull(group);
    }

    @Test
    @DisplayName("[실패] 그룹이 가득 참 검증")
    void validateGroupIsNotFull_Full_ThrowException() {
        // given
        Group group = createMockGroupWithCapacity(10, 10); // 최대 10명, 현재 10명

        // when & then
        assertThatThrownBy(() -> groupValidator.validateGroupIsNotFull(group))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("errorCode", GroupErrorCode.GROUP_FULL);
    }

    @Test
    @DisplayName("[실패] 그룹 정원 초과 검증")
    void validateGroupIsNotFull_Exceeded_ThrowException() {
        // given
        Group group = createMockGroupWithCapacity(10, 11); // 최대 10명, 현재 11명

        // when & then
        assertThatThrownBy(() -> groupValidator.validateGroupIsNotFull(group))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("errorCode", GroupErrorCode.GROUP_FULL);
    }

    @Test
    @DisplayName("[성공] 그룹 소유자 검증 - 강퇴 권한")
    void isOwner_ValidOwner_Success() {
        // given
        Long groupId = 1L;
        Long ownerId = 100L;
        User owner = createMockUser(ownerId);
        Group group = createMockGroupWithOwner(groupId, owner);
        
        given(groupRepository.findByIdGroupId(groupId)).willReturn(Optional.of(group));

        // when
        Group result = groupValidator.isOwner(groupId, ownerId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOwner().getId()).isEqualTo(ownerId);
    }

    @Test
    @DisplayName("[실패] 그룹 소유자 검증 - 강퇴 권한 없음")
    void isOwner_NotOwner_ThrowException() {
        // given
        Long groupId = 1L;
        Long ownerId = 100L;
        Long otherUserId = 200L;
        User owner = createMockUser(ownerId);
        Group group = createMockGroupWithOwner(groupId, owner);
        
        given(groupRepository.findByIdGroupId(groupId)).willReturn(Optional.of(group));

        // when & then
        assertThatThrownBy(() -> groupValidator.isOwner(groupId, otherUserId))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("errorCode", GroupErrorCode.GROUP_KICK_FORBIDDEN);
    }

    @Test
    @DisplayName("[성공] 그룹 소유자 검증 - 삭제 권한")
    void isOwnerForGroupDelete_ValidOwner_Success() {
        // given
        Long groupId = 1L;
        Long ownerId = 100L;
        User owner = createMockUser(ownerId);
        Group group = createMockGroupWithOwner(groupId, owner);
        
        given(groupRepository.findByIdGroupId(groupId)).willReturn(Optional.of(group));

        // when
        Group result = groupValidator.isOwnerForGroupDelete(groupId, ownerId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOwner().getId()).isEqualTo(ownerId);
    }

    @Test
    @DisplayName("[실패] 그룹 소유자 검증 - 삭제 권한 없음")
    void isOwnerForGroupDelete_NotOwner_ThrowException() {
        // given
        Long groupId = 1L;
        Long ownerId = 100L;
        Long otherUserId = 200L;
        User owner = createMockUser(ownerId);
        Group group = createMockGroupWithOwner(groupId, owner);
        
        given(groupRepository.findByIdGroupId(groupId)).willReturn(Optional.of(group));

        // when & then
        assertThatThrownBy(() -> groupValidator.isOwnerForGroupDelete(groupId, otherUserId))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("errorCode", GroupErrorCode.GROUP_DELETE_FORBIDDEN);
    }

    @Test
    @DisplayName("[성공] 그룹 소유자 검증 - 수정 권한")
    void isOwnerForGroupUpdate_ValidOwner_Success() {
        // given
        Long groupId = 1L;
        Long ownerId = 100L;
        User owner = createMockUser(ownerId);
        Group group = createMockGroupWithOwner(groupId, owner);
        
        given(groupRepository.findByIdGroupId(groupId)).willReturn(Optional.of(group));

        // when
        Group result = groupValidator.isOwnerForGroupUpdate(groupId, ownerId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOwner().getId()).isEqualTo(ownerId);
    }

    @Test
    @DisplayName("[실패] 그룹 소유자 검증 - 수정 권한 없음")
    void isOwnerForGroupUpdate_NotOwner_ThrowException() {
        // given
        Long groupId = 1L;
        Long ownerId = 100L;
        Long otherUserId = 200L;
        User owner = createMockUser(ownerId);
        Group group = createMockGroupWithOwner(groupId, owner);
        
        given(groupRepository.findByIdGroupId(groupId)).willReturn(Optional.of(group));

        // when & then
        assertThatThrownBy(() -> groupValidator.isOwnerForGroupUpdate(groupId, otherUserId))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("errorCode", GroupErrorCode.GROUP_UPDATE_FORBIDDEN);
    }

    private Group createMockGroup(Long groupId, GroupStatus status) {
        Group group = mock(Group.class);
        given(group.getId()).willReturn(groupId);
        given(group.getStatus()).willReturn(status);
        return group;
    }

    private Group createMockGroupWithCapacity(int maxCount, int currentCount) {
        Group group = mock(Group.class);
        given(group.getMaxUserCount()).willReturn(maxCount);
        given(group.getCurrentUserCount()).willReturn(currentCount);
        return group;
    }

    private Group createMockGroupWithOwner(Long groupId, User owner) {
        Group group = mock(Group.class);
        // findByIdOrThrow에서 호출되므로 ID와 STATUS 설정 필요
        given(group.getId()).willReturn(groupId);
        given(group.getStatus()).willReturn(GroupStatus.ACTIVE);
        given(group.getOwner()).willReturn(owner);
        return group;
    }

    private User createMockUser(Long userId) {
        User user = mock(User.class);
        given(user.getId()).willReturn(userId);
        return user;
    }
}
