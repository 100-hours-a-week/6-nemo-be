package kr.ai.nemo.domain.groupparticipants.validator;

import java.util.Optional;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
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

  public void validateJoinedParticipant(GroupParticipants groupParticipant) {
    if(groupParticipant.getStatus() == Status.JOINED) {
      throw new GroupException(GroupErrorCode.ALREADY_APPLIED_OR_JOINED);
    }
  }

  public void validateIsJoined(Long groupId, Long userId) {
    if(!repository.existsByGroupIdAndUserIdAndStatus(
        groupId, userId, Status.JOINED)) {
      throw new GroupParticipantException(GroupParticipantErrorCode.NOT_GROUP_MEMBER);
    }
  }

  public boolean validateIsJoinedMember(Long groupId, Long userId) {
    return repository.existsByGroupIdAndUserIdAndStatus(
        groupId, userId, Status.JOINED);
  }

  public Role checkUserRole(CustomUserDetails userDetails, Long groupId) {
    if (userDetails == null) {
      return Role.GUEST;
    }

    Long userId = userDetails.getUserId();

    Optional<GroupParticipants> participant = groupParticipantsRepository
        .findByGroupIdAndUserId(groupId, userId);

    if (participant.isPresent() && participant.get().getStatus() == Status.JOINED) {
      GroupParticipants groupParticipant = participant.get();
      if (groupParticipant.getRole() == Role.LEADER) {
        return Role.LEADER;
      } else {
        return Role.MEMBER;
      }
    } else {
      return Role.NON_MEMBER;
    }
  }

  public GroupParticipants getParticipant(Long groupId, Long userId) {
    GroupParticipants participants = repository.findByGroupIdAndUserId(groupId, userId)
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
