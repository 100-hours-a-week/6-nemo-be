package kr.ai.nemo.domain.group.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import kr.ai.nemo.domain.group.domain.enums.AiMessageType;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.GroupChatbotQuestionRequest;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendRequest;
import kr.ai.nemo.domain.group.dto.response.GroupAiRecommendResponse;
import kr.ai.nemo.domain.group.dto.response.GroupChatbotQuestionResponse;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendQuestionRequest;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendQuestionRequest.Payload;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendRequest.RequestPayload;
import kr.ai.nemo.domain.group.dto.websocket.response.GroupRecommendGroupIdResponse;
import kr.ai.nemo.domain.group.dto.websocket.response.GroupRecommendOptionResponse;
import kr.ai.nemo.domain.group.dto.websocket.response.GroupRecommendQuestionResponse;
import kr.ai.nemo.domain.group.dto.websocket.response.GroupRecommendReasonResponse;
import kr.ai.nemo.global.error.code.CommonErrorCode;
import kr.ai.nemo.global.error.exception.CustomException;
import kr.ai.nemo.global.kafka.producer.GroupChatbotKafkaService;
import kr.ai.nemo.global.kafka.utils.KafkaTopic;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupWebsocketService extends TextWebSocketHandler {

  private final ConcurrentHashMap<String, WebSocketSession> aiConnctions = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, CompletableFuture<GroupChatbotQuestionResponse>> pendingRequests = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, CompletableFuture<GroupAiRecommendResponse>> pendingRecommendRequests = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, StringBuilder> questionCollectors = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, StringBuilder> reasonCollectors = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, List<String>> optionsCollectors = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Long> groupIdCollectors = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final ConcurrentHashMap<String, ScheduledFuture<?>> timeoutTasks = new ConcurrentHashMap<>();
  private final GroupChatbotKafkaService groupChatbotKafkaService;

  private final ObjectMapper objectMapper;

  @Value("${GROUP_CHATBOT_URI}")
  private String groupChatbotUri;

  // session을 만들거나 해제
  public WebSocketSession getOrCreateSessionConnection(String sessionId) {
    return aiConnctions.computeIfAbsent(sessionId, this::createSessionConnection);
  }

  private WebSocketSession createSessionConnection(String sessionId) {
    try {
      WebSocketClient client = new StandardWebSocketClient();
      URI uri = URI.create(groupChatbotUri);

      WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
      headers.add("X-CHATBOT-KEY", sessionId);

      CompletableFuture<WebSocketSession> sessionFuture = client.execute(this, headers, uri);
      WebSocketSession session = sessionFuture.get(10, TimeUnit.SECONDS);

      log.info("Created websocket session: {}", sessionId);
      return session;
    } catch (Exception e) {
      log.error("failed to create AI connection for sessionId {}", sessionId, e);
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }
  }

  // session 종료
  public void closeSessionConnection(String sessionId) {
    WebSocketSession session = aiConnctions.remove(sessionId);
    if (session != null && session.isOpen()) {
      try {
        session.close();
        log.info("AI Websocket session closed for sessionId: {}", sessionId);
      } catch (Exception e) {
        log.error("Error closing AI Websocket sessionId: {}", sessionId, e);
      }
    }
    cleanupSession(sessionId);
  }

  // 질문 생성 요청
  public GroupChatbotQuestionResponse sendQuestionToAI(GroupChatbotQuestionRequest request, Long userId,
      String sessionId) {
    getOrCreateSessionConnection(sessionId);

    try {
      CompletableFuture<GroupChatbotQuestionResponse> future = new CompletableFuture<>();
      pendingRequests.put(sessionId, future);

      ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
        log.warn("Request timeout for session: {}", sessionId);
        future.completeExceptionally(new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR));
        cleanupSession(sessionId);
      }, 5, TimeUnit.MINUTES);

      timeoutTasks.put(sessionId, timeoutTask);

      groupChatbotKafkaService.sendQuestionRequest(request, userId, sessionId);
      /*
      기존 Websocket 코드
      GroupRecommendQuestionRequest aiRequest =
          new GroupRecommendQuestionRequest(
              AiMessageType.CREATE_QUESTION.getValue(), new Payload(sessionId, userId, request.answer()));

      sendAndWaitQuestionAndOptions(session, aiRequest);
       */
      return future.get(6, TimeUnit.MINUTES);
    } catch (Exception e) {
      log.error("Error sending question to AI Websocket sessionId: {}", sessionId, e);
      cleanupSession(sessionId);
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }
  }

  // 모임 추천 요청
  public GroupAiRecommendResponse sendRecommendToAI(GroupAiQuestionRecommendRequest request,
      String sessionId) {
    getOrCreateSessionConnection(sessionId);

    try {
      CompletableFuture<GroupAiRecommendResponse> future = new CompletableFuture<>();
      pendingRecommendRequests.put(sessionId, future);

      groupChatbotKafkaService.sendRecommendRequest(request, sessionId);

      /*
      기존 Websocket 코드
      GroupRecommendRequest aiRequest =
          new GroupRecommendRequest(
              AiMessageType.RECOMMEND_REQUEST.getValue(), new RequestPayload(sessionId, request.userId(), request.messages()));

      sendAndWaitRecommend(session, aiRequest);
       */
      GroupAiRecommendResponse response = future.get(30, TimeUnit.SECONDS);

      cleanupSession(sessionId);
      return response;
    } catch (Exception e) {
      log.error("Error sending question to AI Websocket sessionId: {}", sessionId, e);
      cleanupSession(sessionId);
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }
  }

  /*
   private void sendAndWaitQuestionAndOptions(WebSocketSession session, GroupRecommendQuestionRequest aiRequest) throws Exception {
    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(aiRequest)));
  }

  private void sendAndWaitRecommend(WebSocketSession session, GroupRecommendRequest aiRequest) throws Exception {
    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(aiRequest)));
  }
   */

  private void cleanupSession(String sessionId) {
    log.info("Cleaning up session: {}", sessionId);

    // WebSocket 연결 닫기
    WebSocketSession session = aiConnctions.remove(sessionId);
    if (session != null && session.isOpen()) {
      try {
        session.close();
        log.info("Closed WebSocket session: {}", sessionId);
      } catch (Exception e) {
        log.warn("Error closing session: {}", sessionId, e);
      }
    }

    // 모든 데이터 정리
    questionCollectors.remove(sessionId);
    reasonCollectors.remove(sessionId);
    optionsCollectors.remove(sessionId);
    groupIdCollectors.remove(sessionId);
    pendingRequests.remove(sessionId);
    pendingRecommendRequests.remove(sessionId);

    // 타임아웃 태스크 정리
    ScheduledFuture<?> timeoutTask = timeoutTasks.remove(sessionId);
    if (timeoutTask != null) {
      timeoutTask.cancel(false);
    }
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
    // 1. 일단 공통으로 파싱
    String payload = message.getPayload();

    // 2. type만 먼저 확인
    JsonNode jsonNode = objectMapper.readTree(payload);
    String messageType = jsonNode.get("type").asText();
    String sessionId = jsonNode.get("payload").get("sessionId").asText();

    switch (messageType) {
      case "QUESTION_CHUNK" -> {
        GroupRecommendQuestionResponse response = objectMapper.readValue(payload, GroupRecommendQuestionResponse.class);
        questionCollectors.computeIfAbsent(sessionId, k -> new StringBuilder())
            .append(response.payload().text());
      }

      case "QUESTION_OPTIONS" -> {
        GroupRecommendOptionResponse optionResponse = objectMapper.readValue(payload, GroupRecommendOptionResponse.class);
        List<String> optionsList = optionResponse.payload().options(); // 실제 필드명에 따라
        optionsCollectors.put(sessionId, optionsList);

        // 질문 + 선택지 모두 완료
        completeAiResponse(sessionId);
      }

      case "RECOMMEND_ID" -> {
        GroupRecommendGroupIdResponse response = objectMapper.readValue(payload, GroupRecommendGroupIdResponse.class);
        Long groupId = response.payload().groupId();
        groupIdCollectors.put(sessionId, groupId);
      }

      case "RECOMMEND_REASON" -> {
        GroupRecommendReasonResponse response = objectMapper.readValue(payload, GroupRecommendReasonResponse.class);
        reasonCollectors.computeIfAbsent(sessionId, k -> new StringBuilder())
            .append(response.payload().reason());
      }

      case "RECOMMEND_DONE" ->
        completeRecommendResponse(sessionId);
    }
  }

  private void completeAiResponse(String sessionId) {
    String completeQuestion = questionCollectors.remove(sessionId).toString();
    List<String> options = optionsCollectors.remove(sessionId);

    CompletableFuture<GroupChatbotQuestionResponse> future = pendingRequests.remove(sessionId);
    if (future != null) {
      GroupChatbotQuestionResponse response = new GroupChatbotQuestionResponse(completeQuestion, options);
      future.complete(response);
    }
  }

  private void completeRecommendResponse(String sessionId) {
    Long completeGroupId = groupIdCollectors.remove(sessionId);
    String completeReason  = reasonCollectors.remove(sessionId).toString();

    CompletableFuture<GroupAiRecommendResponse> future = pendingRecommendRequests.remove(sessionId);
    if (future != null) {
      GroupAiRecommendResponse response = new GroupAiRecommendResponse(completeGroupId, completeReason, null);
      future.complete(response);
    }
  }
}
