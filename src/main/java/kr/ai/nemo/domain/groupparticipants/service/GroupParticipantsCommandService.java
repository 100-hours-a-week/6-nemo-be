package kr.ai.nemo.domain.groupparticipants.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import kr.ai.nemo.domain.group.service.GroupCacheService;
import kr.ai.nemo.global.aop.logging.TimeTrace;
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
import kr.ai.nemo.global.aop.role.annotation.DistributedLock;
import kr.ai.nemo.global.redis.CacheKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupParticipantsCommandService {

  private final GroupParticipantsRepository groupParticipantsRepository;
  private final ScheduleParticipantsService scheduleParticipantsService;
  private final GroupValidator groupValidator;
  private final GroupParticipantValidator groupParticipantValidator;
  private final RedisTemplate<String, String> redisTemplate;
  private final GroupCacheService groupCacheService;

  @DistributedLock(
      key = "'cache::group::capacity::' + #groupId",
      waitTime = 2,
      leaseTime = 3,
      timeUnit = TimeUnit.SECONDS
  )
  @TimeTrace
  @Transactional
  public void applyToGroup(Long groupId, CustomUserDetails userDetails, Role role, Status status) {
    User user = userDetails.getUser();
    log.info("applyToGroup: user={} group = {}", user, groupId);
    Group group = groupValidator.findByIdOrThrow(groupId);

    String capacityKey = CacheKeyUtil.key("group", "capacity", groupId);
    int maxCapacity = group.getMaxUserCount();

    Long currentCount = getCachedOrLoadGroupCapacity(capacityKey, group);

    if (currentCount >= maxCapacity) {
      log.info("모임이 가득 찼습니다. userId={}, groupId={}", user.getId(), groupId);
      throw new GroupException(GroupErrorCode.GROUP_FULL);
    }

    boolean isNewParticipant = saveOrRejoinParticipant(user, group, role, status);

    if (isNewParticipant) {
      incrementCapacity(capacityKey);
    }
    group.addCurrentUserCount();
    scheduleParticipantsService.addParticipantToUpcomingSchedules(group, user);
    groupCacheService.evictGroupDetailStatic(groupId);
    groupCacheService.deleteGroupListCaches();
  }

  private Long getCachedOrLoadGroupCapacity(String key, Group group) {
    String rawValue = redisTemplate.opsForValue().get(key);
    if (rawValue != null) return Long.parseLong(rawValue);

    Long count = (long) group.getCurrentUserCount();
    redisTemplate.opsForValue().set(key, String.valueOf(count));
    return count;
  }

  private void incrementCapacity(String key) {
    redisTemplate.opsForValue().increment(key);
  }

  private boolean saveOrRejoinParticipant(User user, Group group, Role role, Status status) {
    Optional<GroupParticipants> participant =
        groupParticipantsRepository.findByGroupIdAndUserId(group.getId(), user.getId());

    if (participant.isPresent()) {
      GroupParticipants existing = participant.get();
      groupParticipantValidator.validateJoinedParticipant(existing);
      existing.rejoin();
      return false;
    } else {
      GroupParticipants newParticipant = GroupParticipants.builder()
          .user(user)
          .group(group)
          .role(role)
          .status(status)
          .appliedAt(LocalDateTime.now())
          .build();
      groupParticipantsRepository.save(newParticipant);
      return true;
    }
  }

  @TimeTrace
  @Transactional
  public void createToGroupLeader(Group group, User user) {
    GroupParticipants newParticipant = GroupParticipants.builder()
        .user(user)
        .group(group)
        .role(Role.LEADER)
        .status(Status.JOINED)
        .appliedAt(LocalDateTime.now())
        .build();
    groupParticipantsRepository.save(newParticipant);
    group.addCurrentUserCount();
  }

  @TimeTrace
  @Transactional
  public void kickOut(Long groupId, Long userId, CustomUserDetails userDetails) {
    Group group = groupValidator.isOwner(groupId, userDetails.getUserId());
    GroupParticipants participants = groupParticipantValidator.getParticipant(groupId, userId);
    groupParticipantValidator.checkOwner(participants);
    participants.setStatus(Status.KICKED);
    group.decreaseCurrentUserCount();
    groupCacheService.evictGroupDetailStatic(groupId);
    groupCacheService.deleteGroupListCaches();
  }

  @TimeTrace
  @Transactional
  public void withdrawGroup(Long groupId, Long userId) {
    Group group = groupValidator.findByIdOrThrow(groupId);
    GroupParticipants participants = groupParticipantValidator.getParticipant(groupId, userId);
    groupParticipantValidator.checkOwner(participants);
    participants.setStatus(Status.WITHDRAWN);
    group.decreaseCurrentUserCount();
    groupCacheService.evictGroupDetailStatic(groupId);
    groupCacheService.deleteGroupListCaches();
  }
}
