package kr.ai.nemo.domain.group.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.fixture.user.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupValidator 테스트")
class GroupValidatorTest {

    @Mock
    private GroupRepository repository;

    @InjectMocks
    private GroupValidator groupValidator;

    @Test
    @DisplayName("[성공] 그룹 조회")
    void findByIdOrThrow_Success() {
        // given
        Long groupId = 1L;
        User owner = UserFixture.createDefaultUser();
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .status(GroupStatus.ACTIVE)
                .currentUserCount(5)
                .maxUserCount(10)
                .build();

        given(repository.findByIdGroupId(groupId)).willReturn(Optional.of(group));

        // when
        Group result = groupValidator.findByIdOrThrow(groupId);

        // then
        assertThat(result).isEqualTo(group);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 그룹 조회")
    void findByIdOrThrow_GroupNotFound_ThrowException() {
        // given
        Long groupId = 999L;
        given(repository.findByIdGroupId(groupId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupValidator.findByIdOrThrow(groupId))
                .isInstanceOf(GroupException.class);
    }

    @Test
    @DisplayName("[실패] 해체된 그룹 조회")
    void findByIdOrThrow_DisbandedGroup_ThrowException() {
        // given
        Long groupId = 1L;
        User owner = UserFixture.createDefaultUser();
        Group disbandedGroup = Group.builder()
                .owner(owner)
                .name("해체된 그룹")
                .status(GroupStatus.DISBANDED)
                .build();

        given(repository.findByIdGroupId(groupId)).willReturn(Optional.of(disbandedGroup));

        // when & then
        assertThatThrownBy(() -> groupValidator.findByIdOrThrow(groupId))
                .isInstanceOf(GroupException.class);
    }

    @Test
    @DisplayName("[성공] 유효한 카테고리 검증")
    void isCategory_ValidCategory_Success() {
        // given
        String validCategory = "IT/개발";

        // when & then
        groupValidator.isCategory(validCategory);
    }

    @Test
    @DisplayName("[실패] 유효하지 않은 카테고리 검증")
    void isCategory_InvalidCategory_ThrowException() {
        // given
        String invalidCategory = "잘못된카테고리";

        // when & then
        assertThatThrownBy(() -> groupValidator.isCategory(invalidCategory))
                .isInstanceOf(GroupException.class);
    }

    @Test
    @DisplayName("[성공] 그룹 정원 여유 있음")
    void validateGroupIsNotFull_HasSpace_Success() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group group = Group.builder()
                .owner(owner)
                .currentUserCount(5)
                .maxUserCount(10)
                .build();

        // when & then
        groupValidator.validateGroupIsNotFull(group); // 예외가 발생하지 않으면 성공
    }

    @Test
    @DisplayName("[실패] 그룹 정원 가득참")
    void validateGroupIsNotFull_GroupFull_ThrowException() {
        // given
        User owner = UserFixture.createDefaultUser();
        Group fullGroup = Group.builder()
                .owner(owner)
                .currentUserCount(10)
                .maxUserCount(10)
                .build();

        // when & then
        assertThatThrownBy(() -> groupValidator.validateGroupIsNotFull(fullGroup))
                .isInstanceOf(GroupException.class);
    }

    @Test
    @DisplayName("[성공] 그룹 소유자 확인")
    void isOwner_CorrectOwner_Success() {
        // given
        Long groupId = 1L;
        Long userId = 1L;
        User owner = User.builder()
                .id(userId)
                .nickname("owner")
                .email("owner@example.com")
                .provider("kakao")
                .providerId("123")
                .build();
        
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .status(GroupStatus.ACTIVE)
                .build();

        given(repository.findByIdGroupId(groupId)).willReturn(Optional.of(group));

        // when
        Group result = groupValidator.isOwner(groupId, userId);

        // then
        assertThat(result).isEqualTo(group);
    }

    @Test
    @DisplayName("[실패] 그룹 소유자 아님")
    void isOwner_NotOwner_ThrowException() {
        // given
        Long groupId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;
        
        User owner = User.builder()
                .id(ownerId)
                .nickname("owner")
                .email("owner@example.com")
                .provider("kakao")
                .providerId("123")
                .build();
        
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .status(GroupStatus.ACTIVE)
                .build();

        given(repository.findByIdGroupId(groupId)).willReturn(Optional.of(group));

        // when & then
        assertThatThrownBy(() -> groupValidator.isOwner(groupId, otherUserId))
                .isInstanceOf(GroupException.class);
    }

    @Test
    @DisplayName("[성공] 그룹 삭제 권한 확인")
    void isOwnerForGroupDelete_CorrectOwner_Success() {
        // given
        Long groupId = 1L;
        Long userId = 1L;
        User owner = User.builder()
                .id(userId)
                .nickname("owner")
                .email("owner@example.com")
                .provider("kakao")
                .providerId("123")
                .build();
        
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .status(GroupStatus.ACTIVE)
                .build();

        given(repository.findByIdGroupId(groupId)).willReturn(Optional.of(group));

        // when
        Group result = groupValidator.isOwnerForGroupDelete(groupId, userId);

        // then
        assertThat(result).isEqualTo(group);
    }

    @Test
    @DisplayName("[실패] 그룹 삭제 권한 없음")
    void isOwnerForGroupDelete_NotOwner_ThrowException() {
        // given
        Long groupId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;
        
        User owner = User.builder()
                .id(ownerId)
                .nickname("owner")
                .email("owner@example.com")
                .provider("kakao")
                .providerId("123")
                .build();
        
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .status(GroupStatus.ACTIVE)
                .build();

        given(repository.findByIdGroupId(groupId)).willReturn(Optional.of(group));

        // when & then
        assertThatThrownBy(() -> groupValidator.isOwnerForGroupDelete(groupId, otherUserId))
                .isInstanceOf(GroupException.class);
    }

    @Test
    @DisplayName("[성공] 그룹 수정 권한 확인")
    void isOwnerForGroupUpdate_CorrectOwner_Success() {
        // given
        Long groupId = 1L;
        Long userId = 1L;
        User owner = User.builder()
                .id(userId)
                .nickname("owner")
                .email("owner@example.com")
                .provider("kakao")
                .providerId("123")
                .build();
        
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .status(GroupStatus.ACTIVE)
                .build();

        given(repository.findByIdGroupId(groupId)).willReturn(Optional.of(group));

        // when
        Group result = groupValidator.isOwnerForGroupUpdate(groupId, userId);

        // then
        assertThat(result).isEqualTo(group);
    }

    @Test
    @DisplayName("[실패] 그룹 수정 권한 없음")
    void isOwnerForGroupUpdate_NotOwner_ThrowException() {
        // given
        Long groupId = 1L;
        Long ownerId = 1L;
        Long otherUserId = 2L;
        
        User owner = User.builder()
                .id(ownerId)
                .nickname("owner")
                .email("owner@example.com")
                .provider("kakao")
                .providerId("123")
                .build();
        
        Group group = Group.builder()
                .owner(owner)
                .name("테스트 그룹")
                .status(GroupStatus.ACTIVE)
                .build();

        given(repository.findByIdGroupId(groupId)).willReturn(Optional.of(group));

        // when & then
        assertThatThrownBy(() -> groupValidator.isOwnerForGroupUpdate(groupId, otherUserId))
                .isInstanceOf(GroupException.class);
    }
}
