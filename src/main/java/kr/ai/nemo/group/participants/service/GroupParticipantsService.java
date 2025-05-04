package kr.ai.nemo.group.participants.service;

import java.time.LocalDateTime;
import java.util.List;
import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.participants.domain.GroupParticipants;
import kr.ai.nemo.group.participants.domain.enums.Role;
import kr.ai.nemo.group.participants.domain.enums.Status;
import kr.ai.nemo.group.participants.repository.GroupParticipantsRepository;
import kr.ai.nemo.group.service.GroupQueryService;
import kr.ai.nemo.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupParticipantsService {

  private final GroupParticipantsRepository groupParticipantsRepository;
  private final UserQueryService userQueryService;
  private final GroupQueryService groupQueryService;

  public void applyToGroup(Long groupId, Long userId) {

    boolean exists = groupParticipantsRepository.existsByGroupIdAndUserIdAndStatusIn(
        groupId, userId, List.of(Status.PENDING, Status.JOINED));

    Group group = groupQueryService.findByIdOrThrow(groupId);

    if (exists) {
      throw new CustomException(ResponseCode.ALREADY_APPLIED_OR_JOINED);
    }

    GroupParticipants groupParticipants = GroupParticipants.builder()
        .user(userQueryService.findByIdOrThrow(userId))
        .group(group)
        .role(Role.MEMBER.getDescription())
        .status(Status.JOINED.getDisplayName())
        .appliedAt(LocalDateTime.now())
        .build();

    groupParticipantsRepository.save(groupParticipants);

    group.addCurrentCount();
  }
}
