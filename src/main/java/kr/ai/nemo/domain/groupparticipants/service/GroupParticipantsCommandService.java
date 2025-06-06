package kr.ai.nemo.domain.groupparticipants.service;

import java.time.LocalDateTime;
import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.repository.GroupParticipantsRepository;
import kr.ai.nemo.domain.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.domain.scheduleparticipants.service.ScheduleParticipantsService;
import kr.ai.nemo.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupParticipantsCommandService {

  private final GroupParticipantsRepository groupParticipantsRepository;
  private final ScheduleParticipantsService scheduleParticipantsService;
  private final GroupValidator groupValidator;
  private final GroupParticipantValidator groupParticipantValidator;

  @TimeTrace
  @Transactional
  public void applyToGroup(Long groupId, CustomUserDetails userDetails, Role role, Status status) {
    User user = userDetails.getUser();
    Group group = groupValidator.findByIdOrThrow(groupId);
    groupParticipantValidator.validateJoinedParticipant(groupId, user.getId());
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

  @TimeTrace
  @Transactional
  public void kickOut(Long groupId, Long userId, CustomUserDetails userDetails) {
    Group group = groupValidator.isOwner(groupId, userDetails.getUserId());
    GroupParticipants participants = groupParticipantValidator.getParticipant(groupId, userId);
    participants.setStatus(Status.KICKED);
    group.decreaseCurrentUserCount();
  }
}
