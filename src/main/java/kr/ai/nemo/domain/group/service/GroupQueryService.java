package kr.ai.nemo.domain.group.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.auth.security.CustomUserDetails;
import kr.ai.nemo.domain.group.domain.Group;
import kr.ai.nemo.domain.group.domain.enums.GroupStatus;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRecommendRequest;
import kr.ai.nemo.domain.group.dto.response.GroupAiRecommendResponse;
import kr.ai.nemo.domain.group.dto.response.GroupChatbotSessionResponse;
import kr.ai.nemo.domain.group.dto.response.GroupDetailResponse;
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
import kr.ai.nemo.global.redis.CacheKeyUtil;
import kr.ai.nemo.global.redis.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
  private final GroupTagService groupTagService;
  private final GroupParticipantValidator groupParticipantValidator;
  private final RedisCacheService redisCacheService;
  private final AiGroupService aiGroupService;

  @Transactional(readOnly = true)
  public GroupListResponse getGroups(GroupSearchRequest request, Pageable pageable) {
    Page<Group> groups;

    if (request.getCategory() != null) {
      groups = groupRepository.findByCategoryAndStatusNot(request.getCategory(),
          GroupStatus.DISBANDED, pageable);
    } else if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
      groups = groupRepository.searchWithKeywordOnly(request.getKeyword(), pageable);
    } else {
      groups = groupRepository.findByStatusNot(GroupStatus.DISBANDED, pageable);
    }

    Page<GroupDto> groupDtoPage = groups.map(GroupDto::from);

    return GroupListResponse.from(groupDtoPage);
  }

  @TimeTrace
  @Transactional(readOnly = true)
  public GroupDetailResponse detailGroup(Long groupId, CustomUserDetails customUserDetails) {
    Group group = groupValidator.findByIdOrThrow(groupId);
    List<String> tags = groupTagService.getTagNamesByGroupId(group.getId());
    Role role = groupParticipantValidator.checkUserRole(customUserDetails, group);
    return GroupDetailResponse.from(group, tags, role);
  }

  @TimeTrace
  @Transactional(readOnly = true)
  public GroupChatbotSessionResponse getChatbotSession(Long userId, String sessionId) {
    // redis에 저장되어 있는 key로 변환
    String redisKey = CacheKeyUtil.key("chatbot", userId, sessionId);

    Optional<JsonNode> sessionJson = redisCacheService.get(redisKey, JsonNode.class);
    if (sessionJson.isEmpty()) {
      return new GroupChatbotSessionResponse(null);
    }

    try {
      // 트리 형태로 저장
      JsonNode root = sessionJson.get();

      // root에서 answers로 저장된 값 꺼내기
      JsonNode answers = root.get("answers");

      if (answers == null || !answers.isArray()) {
        return new GroupChatbotSessionResponse(null);
      }

      List<GroupChatbotSessionResponse.Message> messages = new ArrayList<>();

      for (JsonNode answerNode : answers) {
        String role = answerNode.get("role").asText();
        String text = answerNode.get("text").asText();
        List<String> options = new ArrayList<>();

        JsonNode optionNode = answerNode.get("option");
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
  public GroupRecommendResponse recommendGroup(GroupChatbotSessionResponse session, String sessionId) {

    List<GroupChatbotSessionResponse.Message> messages = session.message();

    if (messages.isEmpty()) {
      throw new GroupException(GroupErrorCode.CHAT_SESSION_NOT_FOUND);
    }

    List<GroupAiQuestionRecommendRequest.ContextLog> contextLogs = messages.stream()
        .map(m -> new GroupAiQuestionRecommendRequest.ContextLog(m.role(), m.text()))
        .toList();

    GroupAiQuestionRecommendRequest aiRequest = new GroupAiQuestionRecommendRequest(contextLogs);

    GroupAiRecommendResponse aiResponse = aiGroupService.recommendGroup(aiRequest, sessionId);
    Group group = groupValidator.findByIdOrThrow(aiResponse.groupId());

    return new GroupRecommendResponse(GroupDto.from(group), aiResponse.responseText());
  }
}
