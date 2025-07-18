package kr.ai.nemo.domain.group.service;

import jakarta.validation.Valid;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kr.ai.nemo.domain.groupparticipants.domain.GroupParticipants;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Status;
import kr.ai.nemo.domain.groupparticipants.repository.GroupParticipantsRepository;
import kr.ai.nemo.domain.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.domain.schedule.domain.Schedule;
import kr.ai.nemo.domain.schedule.domain.enums.ScheduleStatus;
import kr.ai.nemo.domain.schedule.repository.ScheduleRepository;
import kr.ai.nemo.domain.scheduleparticipants.domain.ScheduleParticipant;
import kr.ai.nemo.domain.scheduleparticipants.repository.ScheduleParticipantRepository;
import kr.ai.nemo.global.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.ChatbotRole;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.dto.request.ChatMessage;
import kr.ai.nemo.domain.group.dto.request.GroupAiGenerateRequest;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRequest;
import kr.ai.nemo.domain.group.dto.request.GroupAiRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.GroupChatbotQuestionRequest;
import kr.ai.nemo.domain.group.dto.request.GroupCreateRequest;
import kr.ai.nemo.domain.group.dto.request.GroupGenerateRequest;
import kr.ai.nemo.domain.group.dto.request.GroupRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.UpdateGroupImageRequest;
import kr.ai.nemo.domain.group.dto.response.GroupAiGenerateResponse;
import kr.ai.nemo.domain.group.dto.response.GroupAiRecommendResponse;
import kr.ai.nemo.domain.group.dto.response.GroupRecommendResponse;
import kr.ai.nemo.domain.group.dto.response.GroupChatbotQuestionResponse;
import kr.ai.nemo.domain.group.dto.response.GroupCreateResponse;
import kr.ai.nemo.domain.group.dto.response.GroupDto;
import kr.ai.nemo.domain.group.dto.response.GroupGenerateResponse;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.service.GroupParticipantsCommandService;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.global.redis.CacheConstants;
import kr.ai.nemo.global.redis.CacheKeyUtil;
import kr.ai.nemo.global.redis.RedisCacheService;
import kr.ai.nemo.infra.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupCommandService {

  private final GroupRepository groupRepository;
  private final GroupParticipantsRepository groupParticipantsRepository;
  private final ScheduleRepository scheduleRepository;
  private final ScheduleParticipantRepository scheduleParticipantRepository;
  private final GroupTagService groupTagService;
  private final GroupParticipantsCommandService groupParticipantsCommandService;
  private final GroupCacheService groupCacheService;
  private final AiGroupService aiClient;
  private final ImageService imageService;
  private final GroupValidator groupValidator;
  private final GroupParticipantValidator groupParticipantValidator;
  private final RedisCacheService redisCacheService;
  private final GroupWebsocketService groupWebsocketService;

  @TimeTrace
  @Transactional
  public GroupGenerateResponse generate(GroupGenerateRequest request) {
    GroupAiGenerateRequest aiRequest = GroupAiGenerateRequest.from(request);
    GroupAiGenerateResponse aiResponse = aiClient.call(aiRequest);
    return GroupGenerateResponse.from(request, aiResponse);
  }

  @TimeTrace
  @Transactional
  public GroupCreateResponse createGroup(@Valid GroupCreateRequest request,
      CustomUserDetails userDetails) {

    groupValidator.isCategory(request.category());

    Group group = Group.builder()
        .owner(userDetails.getUser())
        .name(request.name())
        .summary(request.summary())
        .description(request.description())
        .plan(request.plan())
        .category(request.category())
        .location(request.location())
        .completedScheduleTotal(0)
        .imageUrl(imageService.uploadGroupImage(request.imageUrl()))
        .currentUserCount(0)
        .maxUserCount(request.maxUserCount())
        .status(GroupStatus.ACTIVE)
        .build();

    Group savedGroup = groupRepository.saveAndFlush(group);

    if (request.tags() != null && !request.tags().isEmpty()) {
      groupTagService.assignTags(savedGroup, request.tags());
    }

    groupParticipantsCommandService.createToGroupLeader(savedGroup, userDetails.getUser());

    List<String> tags = groupTagService.getTagNamesByGroupId(savedGroup.getId());
    groupCacheService.deleteGroupListCaches();
    return GroupCreateResponse.from(savedGroup, tags);
  }

  @TimeTrace
  @Transactional
  public void deleteGroup(Long groupId, Long userId) {
    Group group = groupValidator.isOwnerForGroupDelete(groupId, userId);
    group.deleteGroup();

    // 2. 모든 그룹 참가자 상태 변경 (JOINED -> WITHDRAWN)
    List<GroupParticipants> participants = groupParticipantsRepository.findByGroupIdAndStatus(groupId, Status.JOINED);
    participants.forEach(GroupParticipants::withdraw);

    // 3. 관련된 모든 일정 상태 변경 (RECRUITING -> CANCELED)
    List<Schedule> schedules = scheduleRepository.findByGroupIdAndStatus(groupId, ScheduleStatus.RECRUITING);
    schedules.forEach(Schedule::cancel);

    // 4. 모든 일정 참가자 삭제 또는 상태 변경
    schedules.forEach(schedule -> {
      List<ScheduleParticipant> scheduleParticipants = scheduleParticipantRepository.findByScheduleId(schedule.getId());
      scheduleParticipantRepository.deleteAll(scheduleParticipants);
    });

    groupCacheService.evictGroupDetailStatic(groupId);
    groupCacheService.deleteGroupListCaches();
  }

  @TimeTrace
  @Transactional
  public void updateGroupImage(Long groupId, Long userId, UpdateGroupImageRequest request) {
    Group group = groupValidator.findByIdOrThrow(groupId);
    groupParticipantValidator.validateIsJoined(groupId, userId);
    group.setImageUrl(imageService.updateImage(group.getImageUrl(), request.imageUrl()));
    group.setUpdatedAt(LocalDateTime.now());
    groupCacheService.evictGroupDetailStatic(groupId);
    groupCacheService.deleteGroupListCaches();
  }

  @TimeTrace
  @Transactional
  public GroupRecommendResponse recommendGroupFreeform(GroupRecommendRequest request,
      Long userId) {
    GroupAiRecommendRequest aiRequest = new GroupAiRecommendRequest(userId, request.requestText());
    GroupAiRecommendResponse aiResponse = aiClient.recommendGroupFreeform(aiRequest);
    GroupDto groupDto = GroupDto.from(aiResponse.group());
    return new GroupRecommendResponse(groupDto, aiResponse.reason());
  }

  @TimeTrace
  @Transactional
  public GroupChatbotQuestionResponse recommendGroupQuestion(GroupChatbotQuestionRequest request,
      Long userId, String sessionId) {
    GroupAiQuestionRequest aiRequest = new GroupAiQuestionRequest(userId, request.answer());
    String redisKey = CacheKeyUtil.key(CacheConstants.REDIS_CHATBOT_PREFIX, userId, sessionId);

    // 사용자 답변이 Null이 아닌 경우 -> 1번째 질문 요청이 아닌 경우 답변을 저장
    if (request.answer() != null) {
      ChatMessage userMsg = new ChatMessage(ChatbotRole.USER, aiRequest.answer());
      redisCacheService.appendToList(redisKey, CacheConstants.REDIS_CHATBOT_MESSAGES_FIELD, userMsg,
          ChatMessage.class, CacheConstants.CHATBOT_SESSION_TTL);
    }

    /*
     기존 코드
     GroupChatbotQuestionResponse aiResponse = aiClient.recommendGroupQuestion(aiRequest, sessionId);
     */

    GroupChatbotQuestionResponse aiResponse = groupWebsocketService.sendQuestionToAI(
        request,
        userId,
        sessionId
    );

    // AI 응답 저장
    ChatMessage aiMsg = new ChatMessage(ChatbotRole.AI, aiResponse.question(),
        aiResponse.options());
    redisCacheService.appendToList(redisKey, CacheConstants.REDIS_CHATBOT_MESSAGES_FIELD, aiMsg,
        ChatMessage.class, CacheConstants.CHATBOT_SESSION_TTL);
    return aiResponse;
  }

  @TimeTrace
  @Transactional
  public String createNewChatbotSession(CustomUserDetails userDetails) {
    String sessionId = UUID.randomUUID().toString();

    Map<String, Object> sessionData = new HashMap<>();
    sessionData.put(CacheConstants.REDIS_CHATBOT_MESSAGES_FIELD, new ArrayList<>());

    String redisKey = CacheKeyUtil.key(CacheConstants.REDIS_CHATBOT_PREFIX,
        userDetails.getUser().getId(), sessionId);
    redisCacheService.set(redisKey, sessionData, Duration.ofMinutes(30));

    return sessionId;
  }
}
