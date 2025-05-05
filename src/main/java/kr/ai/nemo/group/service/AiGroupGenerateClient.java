package kr.ai.nemo.group.service;

import java.time.Duration;
import kr.ai.nemo.common.exception.CustomException;
import kr.ai.nemo.common.exception.ResponseCode;
import kr.ai.nemo.group.dto.GroupAiGenerateRequest;
import kr.ai.nemo.group.dto.GroupAiGenerateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;


@Component
@Slf4j
public class AiGroupGenerateClient {
  private final WebClient webClient;

  public AiGroupGenerateClient(
      @Value("${ai.service.url:http://localhost:5000}") String baseUrl,
      @Value("${ai.service.timeout:5000}") int timeout) {
    this.webClient = WebClient.builder()
        .baseUrl(baseUrl)
        .clientConnector(new ReactorClientHttpConnector(
            HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeout))
        ))
        .build();
  }

  public GroupAiGenerateResponse call(GroupAiGenerateRequest request) {
    log.info("AI 모임 정보 생성 요청: {}", request);
    try {
      return webClient.post()
          .uri("/ai/v1/groups/information")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(request)
          .retrieve()
          .onStatus(HttpStatusCode::is4xxClientError, response -> {
            log.error("AI 서버 클라이언트 오류: {}", response.statusCode());
            return Mono.error(new CustomException(ResponseCode.INVALID_REQUEST));
          })
          .onStatus(HttpStatusCode::is5xxServerError, response -> {
            log.error("AI 서버 서버 오류: {}", response.statusCode());
            return Mono.error(new CustomException(ResponseCode.AI_SERVER_CONNECTION_FAILED));
          })
          .bodyToMono(GroupAiGenerateResponse.class)
          .block();
    } catch (WebClientResponseException e) {
      log.error("AI 서버 응답 오류: {}", e.getMessage());
      throw new CustomException(ResponseCode.AI_SERVER_CONNECTION_FAILED);
    } catch (Exception e) {
      log.error("AI 모임 정보 생성 중 오류 발생: {}", e.getMessage());
      throw new CustomException(ResponseCode.INTERNAL_SERVER_ERROR);
    }
  }
}