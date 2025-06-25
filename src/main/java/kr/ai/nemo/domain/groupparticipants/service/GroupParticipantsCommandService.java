package kr.ai.nemo.domain.groupparticipants.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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

  @DistributedLock(
      key = "'cache::group::capacity::' + #groupId",
      waitTime = 5,
      leaseTime = 3,
      timeUnit = TimeUnit.SECONDS
  )
  @TimeTrace
  @Transactional
  public void applyToGroup(Long groupId, CustomUserDetails userDetails, Role role, Status status) {
    User user = userDetails.getUser();
    Group group = groupValidator.findByIdOrThrow(groupId);

    String capacityKey = CacheKeyUtil.key("group", "capacity", groupId);
    int maxCapacity = group.getMaxUserCount();

    // 1. Redis 초기화
    String rawValue = redisTemplate.opsForValue().get(capacityKey);
    Long cachedCount = (rawValue != null) ? Long.parseLong(rawValue) : null;
    if (cachedCount == null) {
      cachedCount = (long) group.getCurrentUserCount();
      redisTemplate.opsForValue().set(capacityKey, String.valueOf(cachedCount));
    }

// 2. 먼저 DB 기준으로 현재 인원 확인
    if (cachedCount >= maxCapacity) {
      log.info("모임이 가득 찼습니다. userId={}, groupId={}", user.getId(), groupId);
      throw new GroupException(GroupErrorCode.GROUP_FULL);
    }

// 3. 참여자 처리
    Optional<GroupParticipants> participant =
        groupParticipantsRepository.findByGroupIdAndUserId(groupId, user.getId());

    boolean isNewParticipant = false;

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
      isNewParticipant = true;
    }

// 4. 확정된 경우만 Redis & DB 증가
    if (isNewParticipant) {
      redisTemplate.opsForValue().increment(capacityKey);
      group.addCurrentUserCount();
    }

// 5. 향후 일정에도 등록
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
