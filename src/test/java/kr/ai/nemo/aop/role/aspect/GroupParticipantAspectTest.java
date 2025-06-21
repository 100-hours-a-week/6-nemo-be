package kr.ai.nemo.aop.role.aspect;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.groupparticipants.exception.GroupParticipantErrorCode;
import kr.ai.nemo.domain.groupparticipants.exception.GroupParticipantException;
import kr.ai.nemo.domain.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.domain.schedule.dto.request.ScheduleCreateRequest;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupParticipantAspect 테스트")
class GroupParticipantAspectTest {

    @Mock
    private GroupParticipantValidator groupParticipantValidator;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private GroupParticipantAspect groupParticipantAspect;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("[성공] 그룹 멤버십 검증 통과")
    void checkGroupMembership_Success() {
        // given
        Long userId = 1L;
        Long groupId = 100L;
        CustomUserDetails userDetails = createMockUserDetails(userId);
        JoinPoint joinPoint = createMockJoinPoint(groupId);

        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(groupParticipantValidator.validateIsJoinedMember(groupId, userId)).willReturn(true);

        // when & then
        groupParticipantAspect.checkGroupMembership(joinPoint);

        // then
        verify(groupParticipantValidator).validateIsJoinedMember(groupId, userId);
    }

    @Test
    @DisplayName("[실패] 그룹 멤버가 아닌 경우")
    void checkGroupMembership_NotGroupMember_ThrowException() {
        // given
        Long userId = 1L;
        Long groupId = 100L;
        CustomUserDetails userDetails = createMockUserDetails(userId);
        JoinPoint joinPoint = createMockJoinPoint(groupId);

        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(groupParticipantValidator.validateIsJoinedMember(groupId, userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> groupParticipantAspect.checkGroupMembership(joinPoint))
                .isInstanceOf(GroupParticipantException.class)
                .hasFieldOrPropertyWithValue("errorCode", GroupParticipantErrorCode.NOT_GROUP_MEMBER);

        verify(groupParticipantValidator).validateIsJoinedMember(groupId, userId);
    }

    @Test
    @DisplayName("[실패] groupId를 찾을 수 없는 경우")
    void checkGroupMembership_NoGroupId_ThrowException() {
        // given
        Long userId = 1L;
        CustomUserDetails userDetails = createMockUserDetails(userId);
        JoinPoint joinPoint = createMockJoinPointWithoutGroupId();

        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(userDetails);

        // when & then
        assertThatThrownBy(() -> groupParticipantAspect.checkGroupMembership(joinPoint))
                .isInstanceOf(GroupException.class)
                .hasFieldOrPropertyWithValue("errorCode", GroupErrorCode.GROUP_NOT_FOUND);
    }

    private CustomUserDetails createMockUserDetails(Long userId) {
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        given(userDetails.getUserId()).willReturn(userId);
        return userDetails;
    }

    private JoinPoint createMockJoinPoint(Long groupId) {
        JoinPoint joinPoint = mock(JoinPoint.class);
        ScheduleCreateRequest request = mock(ScheduleCreateRequest.class);
        given(request.groupId()).willReturn(groupId);
        given(joinPoint.getArgs()).willReturn(new Object[]{request});
        return joinPoint;
    }

    private JoinPoint createMockJoinPointWithoutGroupId() {
        JoinPoint joinPoint = mock(JoinPoint.class);
        given(joinPoint.getArgs()).willReturn(new Object[]{"invalidArg"});
        return joinPoint;
    }
}
