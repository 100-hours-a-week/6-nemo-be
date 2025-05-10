package kr.ai.nemo.group.service;

import kr.ai.nemo.common.exception.ApiResponse;
import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import kr.ai.nemo.group.dto.GroupAiGenerateRequest;
import kr.ai.nemo.group.dto.GroupAiGenerateResponse;
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

  public AiGroupGenerateClient(
      RestTemplate restTemplate,
      @Value("${ai.service.url:http://localhost:8000}") String baseUrl
  ) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  public GroupAiGenerateResponse call(GroupAiGenerateRequest request) {
    try {
      String url = baseUrl + "/ai/v1/groups/information";

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<GroupAiGenerateRequest> httpEntity = new HttpEntity<>(request, headers);

      ResponseEntity<ApiResponse<GroupAiGenerateResponse>> response = restTemplate.exchange(
          url,
          HttpMethod.POST,
          httpEntity,
          new ParameterizedTypeReference<>() {}
      );

      ApiResponse<GroupAiGenerateResponse> body = response.getBody();

      if (body == null || body.getData() == null) {
        throw new CustomException(ResponseCode.AI_RESPONSE_PARSE_ERROR);
      }

      return body.getData();

    } catch (Exception e) {
      throw new CustomException(ResponseCode.AI_SERVER_CONNECTION_FAILED);
    }
  }
}
