package kr.ai.nemo.group.service;

import kr.ai.nemo.exception.global.CustomException;
import kr.ai.nemo.exception.global.ErrorCode;
import kr.ai.nemo.group.dto.GroupAiGenerateRequest;
import kr.ai.nemo.group.dto.GroupAiGenerateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class AiGroupGenerateClient {

  private final WebClient webClient;

  public AiGroupGenerateClient(@Value("${ai.service.url:http://localhost:5000}") String baseUrl) {
    this.webClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build();
  }

  public GroupAiGenerateResponse call(GroupAiGenerateRequest request) {
    try {
      return webClient.post()
          .uri("/ai/v1/groups/information")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .retrieve()
          .bodyToMono(GroupAiGenerateResponse.class)
          .block();
    } catch (WebClientResponseException e) {
      throw new CustomException(ErrorCode.AI_SERVER_CONNECTION_FAILED);
    } catch (Exception e) {
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }
}