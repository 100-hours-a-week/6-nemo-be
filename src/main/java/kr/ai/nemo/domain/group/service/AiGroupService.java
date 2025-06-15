package kr.ai.nemo.domain.group.service;

import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.group.dto.request.GroupAiDeleteRequest;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRequest;
import kr.ai.nemo.domain.group.dto.request.GroupAiRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.GroupCreateRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiGroupService {

  private final RestTemplate restTemplate;
  private final AiApiProperties aiApiProperties;

  @TimeTrace
  public GroupAiGenerateResponse call(GroupAiGenerateRequest request) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<GroupAiGenerateRequest> httpEntity = new HttpEntity<>(request, headers);

      ResponseEntity<BaseApiResponse<GroupAiGenerateResponse>> response = restTemplate.exchange(
          aiApiProperties.getGroupGenerateUrl(),
          HttpMethod.POST,
          httpEntity,
          new ParameterizedTypeReference<>() {}
      );

      BaseApiResponse<GroupAiGenerateResponse> body = response.getBody();

      if (body == null || body.getData() == null) {
        throw new CustomException(CommonErrorCode.AI_RESPONSE_PARSE_ERROR);
      }

      return body.getData();

    } catch (Exception e) {
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }
  }

  @TimeTrace
  public GroupAiRecommendResponse recommendGroupFreeform(GroupAiRecommendRequest request) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<GroupAiRecommendRequest> httpEntity = new HttpEntity<>(request, headers);

      ResponseEntity<BaseApiResponse<GroupAiRecommendResponse>> response = restTemplate.exchange(
          aiApiProperties.getGroupRecommendFreeformUrl(),
          HttpMethod.POST,
          httpEntity,
          new ParameterizedTypeReference<>() {}
      );

      BaseApiResponse<GroupAiRecommendResponse> body = response.getBody();

      if (body == null || body.getData() == null) {
        throw new CustomException(CommonErrorCode.AI_RESPONSE_PARSE_ERROR);
      }

      return body.getData();
    } catch (Exception e) {
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }
  }

  @TimeTrace
  public GroupChatbotQuestionResponse recommendGroupQuestion(GroupAiQuestionRequest aiRequest, String sessionId) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("X-Session-ID", sessionId);
      HttpEntity<GroupAiQuestionRequest> httpEntity = new HttpEntity<>(aiRequest, headers);

      HttpEntity<BaseApiResponse<GroupChatbotQuestionResponse>> response = restTemplate.exchange(
          aiApiProperties.getGroupRecommendQuestionsUrl(),
          HttpMethod.POST,
          httpEntity,
          new ParameterizedTypeReference<>() {}
      );

      BaseApiResponse<GroupChatbotQuestionResponse> body = response.getBody();

      if (body == null || body.getData() == null) {
        throw new CustomException(CommonErrorCode.AI_RESPONSE_PARSE_ERROR);
      }

      return body.getData();
    } catch (Exception e) {
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }
  }

  @TimeTrace
  public GroupAiRecommendResponse recommendGroup(GroupAiQuestionRecommendRequest aiRequest, String sessionId) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("X-Session-ID", sessionId);
      HttpEntity<GroupAiQuestionRecommendRequest> httpEntity = new HttpEntity<>(aiRequest, headers);

      HttpEntity<BaseApiResponse<GroupAiRecommendResponse>> response = restTemplate.exchange(
          aiApiProperties.getGroupRecommendUrl(),
          HttpMethod.POST,
          httpEntity,
          new ParameterizedTypeReference<>() {}
      );

      BaseApiResponse<GroupAiRecommendResponse> body = response.getBody();

      if (body == null || body.getData() == null) {
        throw new CustomException(CommonErrorCode.AI_RESPONSE_PARSE_ERROR);
      }

      return body.getData();
    } catch (Exception e) {
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }
  }

  @Async
  @TimeTrace
  public void notifyGroupCreated(GroupCreateResponse data) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<GroupCreateResponse> httpEntity = new HttpEntity<>(data, headers);

      restTemplate.postForEntity(aiApiProperties.getGroupCreateUrl(), httpEntity, Void.class);

    } catch (Exception e) {
      log.error("[AI] notifyGroupCreated 호출 중 오류", e);
    }
  }

  @Async
  @TimeTrace
  public void notifyGroupDeleted(Long groupId) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<GroupAiDeleteRequest> httpEntity = new HttpEntity<>(new GroupAiDeleteRequest(groupId), headers);

      restTemplate.postForEntity(aiApiProperties.getGroupDeleteUrl(), httpEntity, Void.class);

    } catch (Exception e) {
      log.error("[AI] notifyGroupDeleted 호출 중 오류", e);
    }
  }

  @Async
  @TimeTrace
  public void notifyGroupJoined(Long userId, Long groupId) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<GroupParticipantAiRequest> httpEntity = new HttpEntity<>(new GroupParticipantAiRequest(userId, groupId), headers);

      restTemplate.postForEntity(aiApiProperties.getGroupJoinUrl(), httpEntity, Void.class);

    } catch (Exception e) {
      log.error("[AI] notifyGroupJoined 호출 중 오류", e);
    }
  }


  @Async
  @TimeTrace
  public void notifyGroupLeft(Long userId, Long groupId) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<GroupParticipantAiRequest> httpEntity = new HttpEntity<>(new GroupParticipantAiRequest(userId, groupId), headers);

      restTemplate.postForEntity(aiApiProperties.getGroupLeaveUrl(), httpEntity, Void.class);

    } catch (Exception e) {
      log.error("[AI] notifyGroupLeft 호출 중 오류", e);
    }
  }
}
