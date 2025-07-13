package kr.ai.nemo.unit.global.aop.role.aspect;

import kr.ai.nemo.domain.auth.security.CustomUserDetails;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class GroupParticipantAspect {

  private final GroupParticipantValidator groupParticipantValidator;

  @Before("@annotation(kr.ai.nemo.unit.global.aop.role.annotation.RequireGroupParticipant)")
  public void checkGroupMembership(JoinPoint joinPoint) {
    CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Long userId = userDetails.getUserId();
    Long groupId = extractGroupId(joinPoint);

    if (!groupParticipantValidator.validateIsJoinedMember(groupId, userId)) {
      throw new GroupParticipantException(GroupParticipantErrorCode.NOT_GROUP_MEMBER);
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
