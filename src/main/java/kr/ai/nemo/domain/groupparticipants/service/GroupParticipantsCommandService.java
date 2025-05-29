package kr.ai.nemo.domain.groupparticipants.service;

import java.time.LocalDateTime;
import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.repository.GroupParticipantsRepository;
import kr.ai.nemo.domain.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.domain.scheduleparticipants.service.ScheduleParticipantsService;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.domain.user.validator.UserValidator;
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

  @TimeTrace
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
