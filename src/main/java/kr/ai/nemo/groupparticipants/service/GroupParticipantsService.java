package kr.ai.nemo.groupparticipants.service;

import java.time.LocalDateTime;
import java.util.List;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.domain.enums.GroupStatus;
import kr.ai.nemo.group.exception.GroupErrorCode;
import kr.ai.nemo.group.exception.GroupException;
import kr.ai.nemo.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.groupparticipants.domain.enums.Role;
import kr.ai.nemo.groupparticipants.domain.enums.Status;
import kr.ai.nemo.groupparticipants.dto.GroupParticipantDto;
import kr.ai.nemo.groupparticipants.dto.MyGroupDto;
import kr.ai.nemo.groupparticipants.exception.GroupParticipantErrorCode;
import kr.ai.nemo.groupparticipants.exception.GroupParticipantException;
import kr.ai.nemo.groupparticipants.repository.GroupParticipantsRepository;
import kr.ai.nemo.group.service.GroupQueryService;
import kr.ai.nemo.scheduleparticipants.service.ScheduleParticipantsService;
import kr.ai.nemo.user.domain.User;
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
  private final ScheduleParticipantsService scheduleParticipantsService;

  @Transactional
  public void applyToGroup(Long groupId, Long userId, Role role, Status status) {
    User user =userQueryService.findByIdOrThrow(userId);

    Group group = groupQueryService.findByIdOrThrow(groupId);

    boolean exists = groupParticipantsRepository.existsByGroupIdAndUserIdAndStatusIn(
        groupId, userId, List.of(Status.PENDING, Status.JOINED));

    if (exists) {
      throw new GroupException(GroupErrorCode.ALREADY_APPLIED_OR_JOINED);
    }
    if (group.getMaxUserCount() <= group.getCurrentUserCount()) {
      throw new GroupException(GroupErrorCode.GROUP_FULL);
    }

    GroupParticipants participant = GroupParticipants.builder()
        .user(user)
        .group(group)
        .role(role)
        .status(status)
        .appliedAt(LocalDateTime.now())
        .build();

    groupParticipantsRepository.save(participant);
    group.addCurrentUserCount();
    scheduleParticipantsService.addParticipantToUpcomingSchedules(group, user);
  }

  public List<GroupParticipantDto> getAcceptedParticipants(Long groupId) {
    groupQueryService.findByIdOrThrow(groupId);
    List<GroupParticipants> participants = groupParticipantsRepository.findByGroupIdAndStatus(groupId, Status.JOINED);
    return participants.stream()
        .map(GroupParticipantDto::from)
        .toList();
  }

  public List<MyGroupDto> getMyGroups(Long userId) {
    List<GroupParticipants> participants = groupParticipantsRepository.findByUserIdAndStatus(userId, Status.JOINED);

    return participants.stream()
        .map(GroupParticipants::getGroup)
        .filter(group -> group.getStatus() != GroupStatus.DISBANDED)
        .map(MyGroupDto::from)
        .toList();
  }

  public void validateJoinedParticipant(Long groupId, Long userId) {
    boolean exists = groupParticipantsRepository.existsByGroupIdAndUserIdAndStatus(
        groupId, userId, Status.JOINED);

    if (!exists) {
      throw new GroupParticipantException(GroupParticipantErrorCode.NOT_GROUP_MEMBER);
    }
  }
}
