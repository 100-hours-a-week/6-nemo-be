package kr.ai.nemo.domain.groupparticipants.service;

import java.time.LocalDateTime;
import java.util.Optional;
import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.repository.GroupParticipantsRepository;
import kr.ai.nemo.domain.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.domain.scheduleparticipants.service.ScheduleParticipantsService;
import kr.ai.nemo.domain.user.domain.User;
import kr.ai.nemo.global.redis.CacheKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupParticipantsCommandService {

  private final GroupParticipantsRepository groupParticipantsRepository;
  private final ScheduleParticipantsService scheduleParticipantsService;
  private final GroupValidator groupValidator;
  private final GroupParticipantValidator groupParticipantValidator;
  private final RedisTemplate<String, String> redisTemplate;

  @TimeTrace
  @Transactional
  public void applyToGroup(Long groupId, CustomUserDetails userDetails, Role role, Status status) {
    User user = userDetails.getUser();
    Group group = groupValidator.findByIdOrThrow(groupId);

    String capacityKey = CacheKeyUtil.key("group", "capacity", groupId);
    int maxCapacity = group.getMaxUserCount();

    Long currentCount = redisTemplate.opsForValue().increment(capacityKey, 0);

    if (currentCount == null) {
      currentCount = (long) group.getCurrentUserCount();
      redisTemplate.opsForValue().set(capacityKey, currentCount.toString());
    }

    if (currentCount > maxCapacity) {
      throw new GroupException(GroupErrorCode.GROUP_FULL);
    }

    // groupValidator.validateGroupIsNotFull(group);

    Optional<GroupParticipants> participant = groupParticipantsRepository.findByGroupIdAndUserId(groupId, user.getId());

    if (participant.isPresent()) {
      GroupParticipants groupParticipant = participant.get();
      groupParticipantValidator.validateJoinedParticipant(groupParticipant);
      groupParticipant.rejoin();

    } else {
      GroupParticipants newParticipant = GroupParticipants.builder()
          .user(user)
          .group(group)
          .role(role)
          .status(status)
          .appliedAt(LocalDateTime.now())
          .build();

      groupParticipantsRepository.save(newParticipant);
    }
    group.addCurrentUserCount();
    scheduleParticipantsService.addParticipantToUpcomingSchedules(group, user);
  }

  @TimeTrace
  @Transactional
  public void kickOut(Long groupId, Long userId, CustomUserDetails userDetails) {
    Group group = groupValidator.isOwner(groupId, userDetails.getUserId());
    GroupParticipants participants = groupParticipantValidator.getParticipant(groupId, userId);
    groupParticipantValidator.checkOwner(participants);
    participants.setStatus(Status.KICKED);
    group.decreaseCurrentUserCount();
  }

  @TimeTrace
  @Transactional
  public void withdrawGroup(Long groupId, Long userId) {
    Group group = groupValidator.findByIdOrThrow(groupId);
    GroupParticipants participants = groupParticipantValidator.getParticipant(groupId, userId);
    groupParticipantValidator.checkOwner(participants);
    participants.setStatus(Status.WITHDRAWN);
    group.decreaseCurrentUserCount();
  }
}
