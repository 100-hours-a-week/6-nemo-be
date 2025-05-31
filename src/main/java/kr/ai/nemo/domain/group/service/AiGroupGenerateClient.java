package kr.ai.nemo.domain.group.service;

import kr.ai.nemo.aop.logging.TimeTrace;
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
public class AiGroupGenerateClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  private static final String GROUP_AI_GENERATE_PATH = "/ai/v1/groups/information";

  public AiGroupGenerateClient(
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
}
