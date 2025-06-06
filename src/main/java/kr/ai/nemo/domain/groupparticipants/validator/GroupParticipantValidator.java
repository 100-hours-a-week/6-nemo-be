package kr.ai.nemo.domain.groupparticipants.validator;

import java.util.List;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.repository.GroupParticipantsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GroupParticipantValidator {

  private final GroupParticipantsRepository repository;

  public void validateJoinedParticipant(Long groupId, Long userId) {
    boolean exists = repository.existsByGroupIdAndUserIdAndStatus(
        groupId, userId, Status.JOINED);

    if (exists) {
      throw new GroupException(GroupErrorCode.ALREADY_APPLIED_OR_JOINED);
    }
  }

  public boolean validateIsJoinedMember(Long groupId, Long userId) {
    return repository.existsByGroupIdAndUserIdAndStatus(
        groupId, userId, Status.JOINED);
  }

  public Role checkUserRole(CustomUserDetails userDetails, Group group) {
    if (userDetails == null) {
      return Role.NON_MEMBER;
    }

    Long userId = userDetails.getUserId();

    if (group.getOwner().getId().equals(userId)) {
      return Role.LEADER;
    } else if (validateIsJoinedMember(group.getId(), userId)) {
      return Role.MEMBER;
    } else {
      return Role.NON_MEMBER;
    }
  }
}
