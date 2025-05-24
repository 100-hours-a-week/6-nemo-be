package kr.ai.nemo.groupparticipants.service;

import java.time.LocalDateTime;
import kr.ai.nemo.group.domain.Group;
import kr.ai.nemo.group.validator.GroupValidator;
import kr.ai.nemo.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.groupparticipants.domain.enums.Role;
import kr.ai.nemo.groupparticipants.domain.enums.Status;
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
public class GroupParticipantsCommandService {

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
}
