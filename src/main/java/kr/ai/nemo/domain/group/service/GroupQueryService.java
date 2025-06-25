package kr.ai.nemo.domain.group.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import kr.ai.nemo.global.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRecommendRequest;
import kr.ai.nemo.domain.group.dto.response.GroupAiRecommendResponse;
import kr.ai.nemo.domain.group.dto.response.GroupChatbotSessionResponse;
import kr.ai.nemo.domain.group.dto.response.GroupDetailResponse;
import kr.ai.nemo.domain.group.dto.response.GroupDetailStaticInfo;
import kr.ai.nemo.domain.group.dto.response.GroupDto;
import kr.ai.nemo.domain.group.dto.response.GroupListResponse;
import kr.ai.nemo.domain.group.dto.request.GroupSearchRequest;
import kr.ai.nemo.domain.group.dto.response.GroupRecommendResponse;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.domain.group.exception.GroupException;
import kr.ai.nemo.domain.group.repository.GroupRepository;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.domain.groupparticipants.domain.enums.Role;
import kr.ai.nemo.domain.groupparticipants.validator.GroupParticipantValidator;
import kr.ai.nemo.global.aop.role.annotation.DistributedLock;
import kr.ai.nemo.global.redis.CacheConstants;
import kr.ai.nemo.global.redis.CacheKeyUtil;
import kr.ai.nemo.global.redis.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GroupQueryService {

  private final GroupRepository groupRepository;
  private final GroupValidator groupValidator;
  private final GroupParticipantValidator groupParticipantValidator;
  private final RedisCacheService redisCacheService;
  private final AiGroupService aiGroupService;
  private final GroupCacheService groupCacheService;

  @TimeTrace
  @Transactional(readOnly = true)
  public GroupListResponse getGroups(GroupSearchRequest request, Pageable pageable) {
    String cacheKey = CacheKeyUtil.key(
        "group-list",
        "category", request.getCategory(),
        "page", pageable.getPageNumber(),
        "size", pageable.getPageSize()
    );

    Optional<GroupListResponse> cached = redisCacheService.get(cacheKey, GroupListResponse.class);
    return cached.orElseGet(() -> loadWithLock(request, pageable, cacheKey));

    // 캐시 미스 → 락 획득 후 처리
  }

  @DistributedLock(
      key = "'category:' + (#request.category == null ? 'null' : #request.category) + " +
          "':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize",
      waitTime = 2,
      leaseTime = 10,
      timeUnit = TimeUnit.SECONDS
  )
  @TimeTrace
  private GroupListResponse loadWithLock(GroupSearchRequest request, Pageable pageable, String cacheKey) {
    // 들어오기 전에 다른 스레드가 캐시 채웠을 수도 있으니 재조회
    Optional<GroupListResponse> cached = redisCacheService.get(cacheKey, GroupListResponse.class);
    if (cached.isPresent()) {
      return cached.get();
    }

    // DB 조회
    Page<Long> groupIdPage;

    if (request.getCategory() != null) {
      log.info("캐시 미스로 인한 카테고리별 DB 조회");
      groupIdPage = groupRepository.findGroupIdsByCategoryAndStatusNot(
          request.getCategory(), GroupStatus.DISBANDED, pageable);
    } else if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
      groupIdPage = groupRepository.searchGroupIdsWithKeywordOnly(
          request.getKeyword(), pageable);
    } else {
      log.info("캐시 미스로 인한 전체 모임 DB 조회");
      groupIdPage = groupRepository.findGroupIdsByStatusNot(pageable);
    }

    List<Group> groups = groupRepository.findGroupsWithTagsByIds(groupIdPage.getContent());

    List<GroupDto> dtos = groups.stream()
        .map(GroupDto::from)
        .toList();

    GroupListResponse result = GroupListResponse.from(
        new PageImpl<>(dtos, pageable, groupIdPage.getTotalElements()));

    // 캐시에 저장
    redisCacheService.set(cacheKey, result, java.time.Duration.ofMinutes(5));

    return result;
  }


  @TimeTrace
  @Transactional(readOnly = true)
  public GroupDetailResponse detailGroup(Long groupId, CustomUserDetails customUserDetails) {
    GroupDetailStaticInfo staticInfo = groupCacheService.getGroupDetailStatic(groupId);
    Group group = groupValidator.findByIdOrThrow(groupId);
    Role role = groupParticipantValidator.checkUserRole(customUserDetails, group);
    return new GroupDetailResponse(
        staticInfo.name(),
        staticInfo.category(),
        staticInfo.summary(),
        staticInfo.description(),
        staticInfo.plan(),
        staticInfo.location(),
        group.getCurrentUserCount(),
        staticInfo.maxUserCount(),
        staticInfo.imageUrl(),
        staticInfo.tags(),
        staticInfo.ownerName(),
        role
    );
  }

  @TimeTrace
  @Transactional(readOnly = true)
  public GroupChatbotSessionResponse getChatbotSession(Long userId, String sessionId) {
    // redis에 저장되어 있는 key로 변환
    String redisKey = CacheKeyUtil.key(CacheConstants.REDIS_CHATBOT_PREFIX, userId, sessionId);

    log.info("getChatbotSession redisKey: {}", redisKey);
    Optional<JsonNode> sessionJson = redisCacheService.get(redisKey, JsonNode.class);
    if (sessionJson.isEmpty()) {
      return new GroupChatbotSessionResponse(null);
    }

    try {
      // 트리 형태로 저장
      JsonNode root = sessionJson.get();

      // root에서 answers로 저장된 값 꺼내기
      JsonNode data = root.get(CacheConstants.REDIS_CHATBOT_MESSAGES_FIELD);
      if (data == null || !data.isArray()) {
        return new GroupChatbotSessionResponse(null);
      }

      List<GroupChatbotSessionResponse.Message> messages = new ArrayList<>();

      for (JsonNode dataNode : data) {
        String role = dataNode.get("role").asText();
        String text = dataNode.get("text").asText();
        List<String> options = new ArrayList<>();

        JsonNode optionNode = dataNode.get("options");
        if (optionNode != null && optionNode.isArray()) {
          for (JsonNode opt : optionNode) {
            options.add(opt.asText());
          }
        }

        messages.add(new GroupChatbotSessionResponse.Message(role, text, options));
      }
      if (messages.isEmpty()) {
        return new GroupChatbotSessionResponse(null);
      }

      return new GroupChatbotSessionResponse(messages);

    } catch (Exception e) {
      log.error("Redis 세션 데이터 파싱 실패: {}", e.getMessage());
      return new GroupChatbotSessionResponse(null);
    }
  }

  @TimeTrace
  @Transactional(readOnly = true)
  public GroupRecommendResponse recommendGroup(Long userId, GroupChatbotSessionResponse session,
      String sessionId) {

    List<GroupChatbotSessionResponse.Message> messages = session.messages();

    if (messages.isEmpty()) {
      throw new GroupException(GroupErrorCode.CHAT_SESSION_NOT_FOUND);
    }

    List<GroupAiQuestionRecommendRequest.ContextLog> contextLogs = messages.stream()
        .map(m -> new GroupAiQuestionRecommendRequest.ContextLog(m.role(), m.text()))
        .toList();

    GroupAiQuestionRecommendRequest aiRequest = new GroupAiQuestionRecommendRequest(userId,
        contextLogs);

    GroupAiRecommendResponse aiResponse = aiGroupService.recommendGroup(aiRequest, sessionId);
    Group group = groupValidator.findByIdOrThrow(aiResponse.groupId());

    return new GroupRecommendResponse(GroupDto.from(group), aiResponse.reason());
  }
}
