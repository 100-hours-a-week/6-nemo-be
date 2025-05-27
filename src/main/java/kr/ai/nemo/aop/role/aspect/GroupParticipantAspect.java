package kr.ai.nemo.aop.role.aspect;

import kr.ai.nemo.domain.auth.exception.AuthErrorCode;
import kr.ai.nemo.domain.auth.exception.AuthException;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.groupparticipants.exception.GroupParticipantErrorCode;
import kr.ai.nemo.domain.groupparticipants.exception.GroupParticipantException;
import kr.ai.nemo.domain.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.domain.schedule.dto.request.ScheduleCreateRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class GroupParticipantAspect {

  private final GroupParticipantValidator groupParticipantValidator;

  @Before("@annotation(kr.ai.nemo.aop.role.annotation.RequireGroupParticipant)")
  public void checkGroupMembership(JoinPoint joinPoint) {
    Long userId = getCurrentUserId();
    Long groupId = extractGroupId(joinPoint);

    if (!groupParticipantValidator.validateIsJoinedMember(groupId, userId)) {
      throw new GroupParticipantException(GroupParticipantErrorCode.NOT_GROUP_MEMBER);
    }
  }

  private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new AuthException(AuthErrorCode.UNAUTHORIZED);
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof Long userId) {
      return userId;
    } else if (principal instanceof String userIdStr) {
      try {
        return Long.parseLong(userIdStr);
      } catch (NumberFormatException e) {
        throw new AuthException(AuthErrorCode.UNAUTHORIZED);
      }
    } else {
      throw new AuthException(AuthErrorCode.UNAUTHORIZED);
    }
  }

  private Long extractGroupId(JoinPoint joinPoint) {
    for (Object arg : joinPoint.getArgs()) {
      if (arg instanceof ScheduleCreateRequest dto) {
        return dto.groupId();
      }
    }
    throw new GroupException(GroupErrorCode.GROUP_NOT_FOUND);
  }
}
