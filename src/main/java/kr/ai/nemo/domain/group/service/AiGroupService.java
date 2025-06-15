package kr.ai.nemo.domain.group.service;

import kr.ai.nemo.aop.logging.TimeTrace;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRequest;
import kr.ai.nemo.domain.group.dto.request.GroupAiRecommendRequest;
import kr.ai.nemo.domain.group.dto.response.GroupAiRecommendResponse;
import kr.ai.nemo.domain.group.dto.response.GroupChatbotQuestionResponse;
import kr.ai.nemo.global.common.BaseApiResponse;
import kr.ai.nemo.global.error.code.CommonErrorCode;
import kr.ai.nemo.global.error.exception.CustomException;
import kr.ai.nemo.domain.group.dto.request.GroupAiGenerateRequest;
import kr.ai.nemo.domain.group.dto.response.GroupAiGenerateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class AiGroupService {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  private static final String GROUP_AI_GENERATE_PATH = "/ai/v1/groups/information";
  private static final String GROUP_RECOMMEND_FREEFORM_PATH = "/ai/v2/groups/recommendations/freeform";
  private static final String GROUP_RECOMMEND_QUESTIONS_PATH = "/ai/v2/groups/recommendations/questions";
  private static final String GROUP_RECOMMEND_PATH = "/ai/v2/groups/recommendations";

  public AiGroupService(
      RestTemplate restTemplate,
      @Value("${ai.service.url:http://localhost:8000}") String baseUrl
  ) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @TimeTrace
  public GroupAiGenerateResponse call(GroupAiGenerateRequest request) {
    try {
      String url = baseUrl + GROUP_AI_GENERATE_PATH;

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<GroupAiGenerateRequest> httpEntity = new HttpEntity<>(request, headers);

      ResponseEntity<BaseApiResponse<GroupAiGenerateResponse>> response = restTemplate.exchange(
          url,
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
      String url = baseUrl + GROUP_RECOMMEND_FREEFORM_PATH;

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<GroupAiRecommendRequest> httpEntity = new HttpEntity<>(request, headers);

      ResponseEntity<BaseApiResponse<GroupAiRecommendResponse>> response = restTemplate.exchange(
          url,
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
      String url = baseUrl + GROUP_RECOMMEND_QUESTIONS_PATH;

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("X-Session-ID", sessionId);
      HttpEntity<GroupAiQuestionRequest> httpEntity = new HttpEntity<>(aiRequest, headers);

      HttpEntity<BaseApiResponse<GroupChatbotQuestionResponse>> response = restTemplate.exchange(
          url,
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

  public GroupAiRecommendResponse recommendGroup(GroupAiQuestionRecommendRequest aiRequest, String sessionId) {
    try {
      String url = baseUrl + GROUP_RECOMMEND_PATH;

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("X-Session-ID", sessionId);
      HttpEntity<GroupAiQuestionRecommendRequest> httpEntity = new HttpEntity<>(aiRequest, headers);

      HttpEntity<BaseApiResponse<GroupAiRecommendResponse>> response = restTemplate.exchange(
          url,
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
}
