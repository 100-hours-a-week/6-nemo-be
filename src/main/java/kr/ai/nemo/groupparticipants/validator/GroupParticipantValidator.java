package kr.ai.nemo.groupparticipants.validator;

import java.util.List;
import kr.ai.nemo.group.exception.GroupErrorCode;
import kr.ai.nemo.group.exception.GroupException;
import kr.ai.nemo.groupparticipants.domain.enums.Status;
import kr.ai.nemo.groupparticipants.repository.GroupParticipantsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GroupParticipantValidator {

  private final GroupParticipantsRepository repository;

  public void validateJoinedParticipant(Long groupId, Long userId) {
    boolean exists = repository.existsByGroupIdAndUserIdAndStatusIn(
        groupId, userId, List.of(Status.PENDING, Status.JOINED));

    if (exists) {
      throw new GroupException(GroupErrorCode.ALREADY_APPLIED_OR_JOINED);
    }
  }
}
