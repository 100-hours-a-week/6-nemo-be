package kr.ai.nemo.domain.group.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.group.dto.request.GroupAiDeleteRequest;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRequest;
import kr.ai.nemo.domain.group.dto.request.GroupAiRecommendRequest;
import kr.ai.nemo.domain.group.dto.response.GroupAiRecommendResponse;
import kr.ai.nemo.domain.group.dto.response.GroupChatbotQuestionResponse;
import kr.ai.nemo.domain.group.dto.response.GroupCreateResponse;
import kr.ai.nemo.domain.groupparticipants.dto.request.GroupParticipantAiRequest;
import kr.ai.nemo.global.common.BaseApiResponse;
import kr.ai.nemo.global.config.AiApiProperties;
import kr.ai.nemo.global.error.code.CommonErrorCode;
import kr.ai.nemo.global.error.exception.CustomException;
import kr.ai.nemo.domain.group.dto.request.GroupAiGenerateRequest;
import kr.ai.nemo.domain.group.dto.response.GroupAiGenerateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiGroupService {

  private final RestClient.Builder restClientBuilder;
  private final AiApiProperties aiApiProperties;
  private final ObjectMapper objectMapper;

  @TimeTrace
  public GroupAiGenerateResponse call(GroupAiGenerateRequest request) {
    return postForData(
        aiApiProperties.getGroupGenerateUrl(),
        request,
        new ParameterizedTypeReference<>() {}
    );
  }

  @TimeTrace
  public GroupAiRecommendResponse recommendGroupFreeform(GroupAiRecommendRequest request) {
    return postForData(
        aiApiProperties.getGroupRecommendFreeformUrl(),
        request,
        new ParameterizedTypeReference<>() {}
    );
  }

  @TimeTrace
  public GroupChatbotQuestionResponse recommendGroupQuestion(GroupAiQuestionRequest aiRequest, String sessionId) {
    return postForDataWithSession(
        aiApiProperties.getGroupRecommendQuestionsUrl(),
        aiRequest,
        sessionId,
        new ParameterizedTypeReference<>() {}
    );
  }

  @TimeTrace
  public GroupAiRecommendResponse recommendGroup(GroupAiQuestionRecommendRequest aiRequest, String sessionId) {
    return postForDataWithSession(
        aiApiProperties.getGroupRecommendUrl(),
        aiRequest,
        sessionId,
        new ParameterizedTypeReference<>() {}
    );
  }

  @Async
  @TimeTrace
  public void notifyGroupCreated(GroupCreateResponse data) {
    try {
      String json = objectMapper.writeValueAsString(data);
      log.info("[요청 JSON] {}", json);
      restClientBuilder.baseUrl(aiApiProperties.getGroupCreateUrl())
          .build()
          .post()
          .contentType(MediaType.APPLICATION_JSON)
          .body(data)
          .retrieve()
          .toBodilessEntity();
    } catch (Exception e) {
      log.error("[AI] notifyGroupCreated 호출 중 오류", e);
    }
  }

  @Async
  @TimeTrace
  public void notifyGroupDeleted(Long groupId) {
    try {
      String json = objectMapper.writeValueAsString(new GroupAiDeleteRequest(groupId));
      log.info("[요청 JSON] {}", objectMapper.writeValueAsString(json));
      restClientBuilder.baseUrl(aiApiProperties.getGroupDeleteUrl())
          .build()
          .post()
          .contentType(MediaType.APPLICATION_JSON)
          .body(new GroupAiDeleteRequest(groupId))
          .retrieve()
          .toBodilessEntity();
    } catch (Exception e) {
      log.error("[AI] notifyGroupDeleted 호출 중 오류", e);
    }
  }

  @Async
  @TimeTrace
  public void notifyGroupJoined(Long userId, Long groupId) {
    try {
      restClientBuilder.baseUrl(aiApiProperties.getGroupJoinUrl())
          .build()
          .post()
          .contentType(MediaType.APPLICATION_JSON)
          .body(new GroupParticipantAiRequest(userId, groupId))
          .retrieve()
          .toBodilessEntity();
    } catch (Exception e) {
      log.error("[AI] notifyGroupJoined 호출 중 오류", e);
    }
  }

  @Async
  @TimeTrace
  public void notifyGroupLeft(Long userId, Long groupId) {
    try {
      restClientBuilder.baseUrl(aiApiProperties.getGroupLeaveUrl())
          .build()
          .post()
          .contentType(MediaType.APPLICATION_JSON)
          .body(new GroupParticipantAiRequest(userId, groupId))
          .retrieve()
          .toBodilessEntity();
    } catch (Exception e) {
      log.error("[AI] notifyGroupLeft 호출 중 오류", e);
    }
  }

  // 세션 없는 post 요청
  private <T, R> R postForData(String url, T requestBody, ParameterizedTypeReference<BaseApiResponse<R>> typeRef) {
    try {
      BaseApiResponse<R> response = restClientBuilder.baseUrl(url).build()
          .post()
          .contentType(MediaType.APPLICATION_JSON)
          .body(requestBody)
          .retrieve()
          .body(typeRef);

      if (response == null || response.getData() == null) {
        throw new CustomException(CommonErrorCode.AI_RESPONSE_PARSE_ERROR);
      }

      return response.getData();

    } catch (Exception e) {
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }
  }

  // 세션 있는 post 요청
  private <T, R> R postForDataWithSession(String url, T requestBody, String sessionId,
      ParameterizedTypeReference<BaseApiResponse<R>> typeRef) {
    try {
      BaseApiResponse<R> response = restClientBuilder.baseUrl(url).build()
          .post()
          .contentType(MediaType.APPLICATION_JSON)
          .header("X-Session-ID", sessionId)
          .body(requestBody)
          .retrieve()
          .body(typeRef);

      if (response == null || response.getData() == null) {
        throw new CustomException(CommonErrorCode.AI_RESPONSE_PARSE_ERROR);
      }

      return response.getData();

    } catch (Exception e) {
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }
  }
}
