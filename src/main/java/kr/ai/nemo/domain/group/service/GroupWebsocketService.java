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
import kr.ai.nemo.domain.group.domain.enums.ChatbotRole;
import kr.ai.nemo.domain.group.dto.request.ChatMessage;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.GroupChatbotQuestionRequest;
import kr.ai.nemo.domain.group.dto.response.GroupAiRecommendResponse;
import kr.ai.nemo.domain.group.dto.response.GroupChatbotQuestionResponse;
import kr.ai.nemo.domain.group.dto.response.GroupDto;
import kr.ai.nemo.domain.group.dto.sse.response.SseDoneResponse;
import kr.ai.nemo.domain.group.dto.sse.response.SseGroupQuestionOptionResponse;
import kr.ai.nemo.domain.group.dto.sse.response.SseGroupQuestionOptionResponse.Option;
import kr.ai.nemo.domain.group.dto.sse.response.SseGroupQuestionResponse;
import kr.ai.nemo.domain.group.dto.sse.response.SseGroupRecommendReasonResponse;
import kr.ai.nemo.domain.group.dto.sse.response.SseGroupRecommendReasonResponse.Reason;
import kr.ai.nemo.domain.group.dto.sse.response.SseGroupRecommendResponse;
import kr.ai.nemo.domain.group.dto.sse.response.SseErrorResponse;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendQuestionRequest;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendQuestionRequest.Payload;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendRequest;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendRequest.RequestPayload;
import kr.ai.nemo.domain.group.dto.websocket.response.GroupRecommendGroupIdResponse;
import kr.ai.nemo.domain.group.dto.websocket.response.GroupRecommendOptionResponse;
import kr.ai.nemo.domain.group.dto.websocket.response.GroupRecommendQuestionResponse;
import kr.ai.nemo.domain.group.dto.websocket.response.GroupRecommendReasonResponse;
import kr.ai.nemo.domain.group.messaging.GroupChatbotMessagePublisher;
import kr.ai.nemo.domain.group.validator.GroupValidator;
import kr.ai.nemo.global.error.code.CommonErrorCode;
import kr.ai.nemo.global.error.exception.CustomException;
import kr.ai.nemo.global.redis.CacheConstants;
import kr.ai.nemo.global.redis.CacheKeyUtil;
import kr.ai.nemo.global.redis.RedisCacheService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
  private final GroupChatbotMessagePublisher groupChatbotMessagePublisher;

  private final ConcurrentHashMap<String, ChatbotSseService> sseServices = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Long> userIds = new ConcurrentHashMap<>();

  private final ObjectMapper objectMapper;
  private final GroupValidator groupValidator;
  private final RedisCacheService redisCacheService;

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
  public GroupChatbotQuestionResponse sendQuestionToAI(GroupChatbotQuestionRequest request,
      Long userId,
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

      groupChatbotMessagePublisher.publishQuestionRequest(request, userId, sessionId);

      return future.get(6, TimeUnit.MINUTES);
    } catch (Exception e) {
      log.error("Error sending question to AI Websocket sessionId: {}", sessionId, e);
      cleanupSession(sessionId);
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }
  }

  public void sendQuestionToAIWithStream(GroupChatbotQuestionRequest request, Long userId,
      String sessionId, ChatbotSseService sseService) {
    WebSocketSession session = getOrCreateSessionConnection(sessionId);

    // SSE 관련 정보 저장
    sseServices.put(sessionId, sseService);
    userIds.put(sessionId, userId);

    try {
      // 타임아웃 설정 (SSE용)
      ScheduledFuture<?> timeoutTask = scheduler.schedule(() -> {
        log.warn("SSE 스트리밍 타임아웃 for session: {}", sessionId);
        // SSE 에러 전송
        ChatbotSseService service = sseServices.get(sessionId);
        Long uid = userIds.get(sessionId);
        if (service != null && uid != null) {
          service.streamAiResponse(uid, sessionId, new SseErrorResponse(AiMessageType.ERROR, CommonErrorCode.SSE_ERROR.getMessage()));
        }
        cleanupSseSession(sessionId); // SSE 전용 정리
      }, 5, TimeUnit.MINUTES);

      timeoutTasks.put(sessionId, timeoutTask);

      GroupRecommendQuestionRequest aiRequest =
          new GroupRecommendQuestionRequest(
              AiMessageType.CREATE_QUESTION.getValue(),
              new Payload(sessionId, userId, request.answer()));

      // AI에 전송
      sendQuestionRequest(session, aiRequest);

      log.info("SSE 스트리밍용 질문 요청 전송 완료 - 세션: {}, 사용자: {}", sessionId, userId);

      // return 제거 - void 메서드이므로 응답 대기하지 않음

    } catch (Exception e) {
      log.error("Error sending SSE question to AI Websocket sessionId: {}", sessionId, e);
      // SSE 에러 전송
      ChatbotSseService service = sseServices.get(sessionId);
      Long uid = userIds.get(sessionId);
      if (service != null && uid != null) {
        service.streamAiResponse(uid, sessionId, new SseErrorResponse(AiMessageType.ERROR, CommonErrorCode.SSE_ERROR.getMessage()));
      }
      cleanupSseSession(sessionId);
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }

    /*
    kafka 잠시 해제
    getOrCreateSessionConnection(sessionId);

    // SSE 관련 정보 저장
    sseServices.put(sessionId, sseService);
    userIds.put(sessionId, userId);

    try {
      groupChatbotMessagePublisher.publishQuestionRequest(request, userId, sessionId);
      log.info("SSE 스트리밍용 질문 요청 전송 - 세션: {}, 사용자: {}", sessionId, userId);
    } catch (Exception e) {
      log.error("SSE 질문 요청 실패 - 세션: {}", sessionId, e);
      // 실패 시 정리
      sseServices.remove(sessionId);
      userIds.remove(sessionId);
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }
     */
  }

  public void sendRecommendToAIWithStream(GroupAiQuestionRecommendRequest request, String sessionId, ChatbotSseService sseService) {
    WebSocketSession session = getOrCreateSessionConnection(sessionId);

    // SSE 관련 정보 저장
    sseServices.put(sessionId, sseService);
    userIds.put(sessionId, request.userId());

    try {
      // 기존 로직과 동일한 요청 객체 생성
      GroupRecommendRequest aiRequest = new GroupRecommendRequest(
          AiMessageType.RECOMMEND_REQUEST.getValue(),
          new RequestPayload(sessionId, request.userId(), request.messages()));

      session.sendMessage(new TextMessage(objectMapper.writeValueAsString(aiRequest)));

      log.info("SSE 스트리밍용 모임 추천 요청 전송 - 세션: {}, 사용자: {}", sessionId, request.userId());

    } catch (Exception e) {
      log.error("SSE 모임 추천 요청 실패 - 세션: {}", sessionId, e);
      sseServices.remove(sessionId);
      userIds.remove(sessionId);
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }
  }

  // SSE 전용 정리 메서드
  private void cleanupSseSession(String sessionId) {
    sseServices.remove(sessionId);
    userIds.remove(sessionId);

    // 타임아웃 태스크 정리
    ScheduledFuture<?> timeoutTask = timeoutTasks.remove(sessionId);
    if (timeoutTask != null) {
      timeoutTask.cancel(false);
    }

    log.info("SSE 세션 정리 완료: {}", sessionId);
  }

  private void sendQuestionRequest(WebSocketSession session,
      GroupRecommendQuestionRequest aiRequest) {
    try {
      String message = objectMapper.writeValueAsString(aiRequest);
      session.sendMessage(new TextMessage(message));
    } catch (Exception e) {
      log.error("WebSocket 메시지 전송 실패", e);
      throw new RuntimeException("메시지 전송 실패", e);
    }
  }

  // 모임 추천 요청
  public GroupAiRecommendResponse sendRecommendToAI(GroupAiQuestionRecommendRequest request,
      String sessionId) {
    getOrCreateSessionConnection(sessionId);

    try {
      CompletableFuture<GroupAiRecommendResponse> future = new CompletableFuture<>();
      pendingRecommendRequests.put(sessionId, future);

      groupChatbotMessagePublisher.publishRecommendRequest(request, sessionId);

      GroupAiRecommendResponse response = future.get(30, TimeUnit.SECONDS);

      cleanupSession(sessionId);
      return response;
    } catch (Exception e) {
      log.error("Error sending question to AI Websocket sessionId: {}", sessionId, e);
      cleanupSession(sessionId);
      throw new CustomException(CommonErrorCode.AI_SERVER_CONNECTION_FAILED);
    }
  }

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

    sseServices.remove(sessionId);
    userIds.remove(sessionId);

    // 타임아웃 태스크 정리
    ScheduledFuture<?> timeoutTask = timeoutTasks.remove(sessionId);
    if (timeoutTask != null) {
      timeoutTask.cancel(false);
    }
  }

  // AI가 응답
  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message)
      throws Exception {
    // 1. 일단 공통으로 파싱
    String payload = message.getPayload();

    // 2. type만 먼저 확인
    JsonNode jsonNode = objectMapper.readTree(payload);
    String messageType = jsonNode.get("type").asText();
    String sessionId = jsonNode.get("payload").get("sessionId").asText();

    switch (messageType) {
      case "QUESTION_CHUNK" -> {
        GroupRecommendQuestionResponse response = objectMapper.readValue(payload,
            GroupRecommendQuestionResponse.class);
        String chunk = response.payload().text();

        // 1. 기존 HTTP 응답용 (수집)
        questionCollectors.computeIfAbsent(sessionId, k -> new StringBuilder()).append(chunk);

        // 2. SSE 스트리밍 (즉시 전송)
        ChatbotSseService sseService = sseServices.get(sessionId);
        Long userId = userIds.get(sessionId);
        if (sseService != null && userId != null) {
          sseService.streamAiResponse(userId, sessionId, new SseGroupQuestionResponse(AiMessageType.QUESTION_CHUNK, new SseGroupQuestionResponse.Payload(chunk)));
        }
      }

      case "QUESTION_OPTIONS" -> {
        GroupRecommendOptionResponse optionResponse = objectMapper.readValue(payload,
            GroupRecommendOptionResponse.class);
        String completeQuestion = questionCollectors.get(sessionId).toString();
        List<String> optionsList = optionResponse.payload().options(); // 실제 필드명에 따라
        optionsCollectors.put(sessionId, optionsList);

        Long userId = userIds.get(sessionId);
        if (userId != null) {
          String redisKey = CacheKeyUtil.key(CacheConstants.REDIS_CHATBOT_PREFIX, userId, sessionId);
          ChatMessage aiMsg = new ChatMessage(ChatbotRole.AI, completeQuestion, optionsList);
          redisCacheService.appendToList(redisKey, CacheConstants.REDIS_CHATBOT_MESSAGES_FIELD, aiMsg,
              ChatMessage.class, CacheConstants.CHATBOT_SESSION_TTL);
        }

        ChatbotSseService sseService = sseServices.get(sessionId);
        if (sseService != null && userId != null) {
          sseService.streamAiResponse(userId, sessionId, new SseGroupQuestionOptionResponse(AiMessageType.QUESTION_OPTIONS, new Option(optionsList)));
          sseService.streamAiResponse(userId, sessionId, new SseDoneResponse(AiMessageType.QUESTION_DONE, null));
        }
      }

      case "RECOMMEND_ID" -> {
        GroupRecommendGroupIdResponse response = objectMapper.readValue(payload,
            GroupRecommendGroupIdResponse.class);
        Long groupId = response.payload().groupId();
        groupIdCollectors.put(sessionId, groupId);

        ChatbotSseService sseService = sseServices.get(sessionId);
        Long userId = userIds.get(sessionId);
        if (sseService != null && userId != null) {
          sseService.streamAiResponse(userId, sessionId, new SseGroupRecommendResponse(AiMessageType.RECOMMEND_ID,
              GroupDto.from(groupValidator.findByIdOrThrow(groupId))));
        }
      }

      case "RECOMMEND_REASON" -> {
        GroupRecommendReasonResponse response = objectMapper.readValue(payload,
            GroupRecommendReasonResponse.class);
        String chunk = response.payload().reason();

        // reasonCollectors에 저장 (completeRecommendResponse에서 사용)
        reasonCollectors.computeIfAbsent(sessionId, k -> new StringBuilder()).append(chunk);

        ChatbotSseService sseService = sseServices.get(sessionId);
        Long userId = userIds.get(sessionId);
        if (sseService != null && userId != null) {
          sseService.streamAiResponse(userId, sessionId, new SseGroupRecommendReasonResponse(AiMessageType.RECOMMEND_REASON, new Reason(chunk)));
        }
      }

      case "RECOMMEND_DONE" -> {
        ChatbotSseService sseService = sseServices.get(sessionId);
        Long userId = userIds.get(sessionId);
        if (sseService != null && userId != null) {
          sseService.streamAiResponse(userId, sessionId, new SseDoneResponse(AiMessageType.RECOMMEND_DONE, null));
          sseService.disconnectStream(userId, sessionId); // 연결 끊기

          // 정리
          sseServices.remove(sessionId);
          userIds.remove(sessionId);
        }
      }
    }
  }

  /*
  SSE 사용으로 불필요

  private void completeAiResponse(String sessionId) {
    String completeQuestion = questionCollectors.remove(sessionId).toString();
    List<String> options = optionsCollectors.remove(sessionId);

    CompletableFuture<GroupChatbotQuestionResponse> future = pendingRequests.remove(sessionId);
    if (future != null) {
      GroupChatbotQuestionResponse response = new GroupChatbotQuestionResponse(completeQuestion,
          options);
      future.complete(response);
    }
  }


  private void completeRecommendResponse(String sessionId) {
    Long completeGroupId = groupIdCollectors.remove(sessionId);

    // NPE 방지: null 체크 추가
    StringBuilder reasonBuilder = reasonCollectors.remove(sessionId);
    String completeReason = reasonBuilder != null ? reasonBuilder.toString() : "";

    log.info("추천 응답 완료 - 세션: {}, 그룹ID: {}, 이유 길이: {}",
        sessionId, completeGroupId, completeReason.length());

    CompletableFuture<GroupAiRecommendResponse> future = pendingRecommendRequests.remove(sessionId);
    if (future != null) {
      GroupAiRecommendResponse response = new GroupAiRecommendResponse(completeGroupId,
          completeReason, null);
      future.complete(response);
    } else {
      log.warn("pendingRecommendRequests에서 해당 세션을 찾을 수 없음: {}", sessionId);
    }
  }
   */
}
