package kr.ai.nemo.domain.groupparticipants.validator;

import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.exception.GroupParticipantErrorCode;
import kr.ai.nemo.domain.groupparticipants.exception.GroupParticipantException;
import kr.ai.nemo.domain.groupparticipants.repository.GroupParticipantsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GroupParticipantValidator {

  private final GroupParticipantsRepository repository;
  private final GroupParticipantsRepository groupParticipantsRepository;

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
      return Role.GUEST;
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

  public GroupParticipants getParticipant(Long groupId, Long userId) {
    GroupParticipants participants = groupParticipantsRepository.findByGroupIdAndUserId(groupId, userId)
        .orElseThrow(() -> new GroupParticipantException(GroupParticipantErrorCode.NOT_GROUP_MEMBER));
    if(participants.getStatus()==Status.KICKED){
      throw new GroupParticipantException(GroupParticipantErrorCode.ALREADY_KICKED_MEMBER);
    } else if(participants.getStatus()==Status.WITHDRAWN){
      throw new GroupParticipantException(GroupParticipantErrorCode.ALREADY_WITHDRAWN_MEMBER);
    }
    return participants;
  }

  public void checkOwner(GroupParticipants participants) {
    if(participants.getRole()==Role.LEADER){
      throw new GroupParticipantException(GroupParticipantErrorCode.LEADER_CANNOT_BE_REMOVED);
    }
  }
}
