package kr.ai.nemo.group.participants.service;

import java.time.LocalDateTime;
import java.util.List;
import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.participants.domain.GroupParticipants;
import kr.ai.nemo.group.participants.domain.enums.Role;
import kr.ai.nemo.group.participants.domain.enums.Status;
import kr.ai.nemo.group.participants.dto.GroupParticipantDto;
import kr.ai.nemo.group.participants.dto.MyGroupDto;
import kr.ai.nemo.group.participants.repository.GroupParticipantsRepository;
import kr.ai.nemo.group.service.GroupQueryService;
import kr.ai.nemo.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupParticipantsService {

  private final GroupParticipantsRepository groupParticipantsRepository;
  private final UserQueryService userQueryService;
  private final GroupQueryService groupQueryService;

  @Transactional
  public void applyToGroup(Long groupId, Long userId, Role role, Status status) {
    boolean exists = groupParticipantsRepository.existsByGroupIdAndUserIdAndStatusIn(
        groupId, userId, List.of(Status.PENDING, Status.JOINED));

    Group group = groupQueryService.findByIdOrThrow(groupId);

    if (exists) {
      throw new CustomException(ResponseCode.ALREADY_APPLIED_OR_JOINED);
    }

    GroupParticipants participant = GroupParticipants.builder()
        .user(userQueryService.findByIdOrThrow(userId))
        .group(group)
        .role(role)
        .status(status)
        .appliedAt(LocalDateTime.now())
        .build();

    groupParticipantsRepository.save(participant);
    group.addCurrentCount();
  }

  public List<GroupParticipantDto> getAcceptedParticipants(Long groupId) {
    groupQueryService.findByIdOrThrow(groupId);
    List<GroupParticipants> participants = groupParticipantsRepository.findByGroupIdAndStatus(groupId, Status.JOINED);
    return participants.stream()
        .map(p -> GroupParticipantDto.from(p.getUser()))
        .toList();
  }

  public List<MyGroupDto> getMyGroups(Long userId) {
    List<GroupParticipants> participants = groupParticipantsRepository.findByUserIdAndStatus(userId, Status.JOINED);
    return participants.stream()
        .map(p -> MyGroupDto.from(p.getGroup()))
        .toList();
  }
}
