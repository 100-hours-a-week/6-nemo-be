package kr.ai.nemo.groupparticipants.service;

import java.time.LocalDateTime;
import java.util.List;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.domain.enums.GroupStatus;
import kr.ai.nemo.group.validator.GroupValidator;
import kr.ai.nemo.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.groupparticipants.domain.enums.Role;
import kr.ai.nemo.groupparticipants.domain.enums.Status;
import kr.ai.nemo.groupparticipants.dto.GroupParticipantDto;
import kr.ai.nemo.groupparticipants.dto.MyGroupDto;
import kr.ai.nemo.groupparticipants.repository.GroupParticipantsRepository;
import kr.ai.nemo.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.scheduleparticipants.service.ScheduleParticipantsService;
import kr.ai.nemo.user.domain.User;
import kr.ai.nemo.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupParticipantsService {

  private final GroupParticipantsRepository groupParticipantsRepository;
  private final ScheduleParticipantsService scheduleParticipantsService;
  private final GroupValidator groupValidator;
  private final UserValidator userValidator;
  private final GroupParticipantValidator groupParticipantValidator;

  @Transactional
  public void applyToGroup(Long groupId, Long userId, Role role, Status status) {
    User user =userValidator.findByIdOrThrow(userId);
    Group group = groupValidator.findByIdOrThrow(groupId);
    groupParticipantValidator.validateJoinedParticipant(groupId, userId);
    groupValidator.validateGroupIsNotFull(group);

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
    groupValidator.findByIdOrThrow(groupId);
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
}
