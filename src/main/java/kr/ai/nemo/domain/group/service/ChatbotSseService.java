package kr.ai.nemo.domain.group.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
import kr.ai.nemo.domain.group.dto.response.GroupChatbotSessionResponse;
import kr.ai.nemo.domain.group.dto.response.GroupChatbotSessionResponse.Message;
import kr.ai.nemo.domain.group.dto.sse.response.SseErrorResponse;
import kr.ai.nemo.domain.group.dto.sse.response.SsePingResponse;
import kr.ai.nemo.domain.group.exception.GroupErrorCode;
import kr.ai.nemo.unit.global.redis.CacheConstants;
import kr.ai.nemo.unit.global.redis.CacheKeyUtil;
import kr.ai.nemo.unit.global.redis.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotSseService {

  private final GroupWebsocketService groupWebsocketService;
  private final RedisCacheService redisCacheService;
  private final GroupQueryService groupQueryService;
  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
  
  // Ping 관련 추가
  private final ScheduledExecutorService pingScheduler = Executors.newScheduledThreadPool(5);
  private final Map<String, ScheduledFuture<?>> pingTasks = new ConcurrentHashMap<>();
  
  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 10; // SSE 연결 유지 시간 10분
  private static final Long PING_INTERVAL = 30L * 1000; // 30초마다 ping

  // 사용자 답변 Redis에 저장 후 WebSocket 연결
  public void processQuestionWithStream(GroupChatbotQuestionRequest request, Long userId, String sessionId) {

    if (request != null) {
      String redisKey = CacheKeyUtil.key(CacheConstants.REDIS_CHATBOT_PREFIX, userId, sessionId);
      ChatMessage userMsg = new ChatMessage(ChatbotRole.USER, request.answer());
      redisCacheService.appendToList(redisKey, CacheConstants.REDIS_CHATBOT_MESSAGES_FIELD, userMsg,
          ChatMessage.class, CacheConstants.CHATBOT_SESSION_TTL);
    }

    groupWebsocketService.sendQuestionToAIWithStream(request, userId, sessionId, this);
  }

  // 모임 추천 요청 (SSE 스트리밍)
  public void processRecommendWithStream(Long userId, String sessionId) {
    log.info("모임 추천 요청 시작 - 세션: {}, 사용자: {}", sessionId, userId);

    try {
      // 1. 세션에서 메시지 히스토리 가져오기 (기존 로직 활용)
      GroupChatbotSessionResponse session = groupQueryService.getChatbotSession(userId, sessionId);

      List<Message> messages = session.messages();
      if (messages == null || messages.isEmpty()) {
        streamAiResponse(userId, sessionId, new SseErrorResponse(AiMessageType.ERROR,
            GroupErrorCode.CHAT_SESSION_NOT_FOUND.getMessage()));
        return;
      }

      // 2. ContextLog 변환 (기존 로직 활용)
      List<GroupAiQuestionRecommendRequest.ContextLog> contextLogs = messages.stream()
          .map(m -> new GroupAiQuestionRecommendRequest.ContextLog(m.role(), m.text()))
          .toList();

      // 3. AI 요청 객체 생성 (기존 로직 활용)
      GroupAiQuestionRecommendRequest aiRequest = new GroupAiQuestionRecommendRequest(userId, contextLogs);

      // 4. SSE 스트리밍으로 AI에 요청
      groupWebsocketService.sendRecommendToAIWithStream(aiRequest, sessionId, this);

    } catch (Exception e) {
      log.error("모임 추천 요청 실패 - 세션: {}", sessionId, e);
      streamAiResponse(userId, sessionId, new SseEmitter(DEFAULT_TIMEOUT));
    }
  }


  public SseEmitter createStream(Long userId, String sessionId) {
    String streamKey = createStreamKey(userId, sessionId);

    SseEmitter existingEmitter = emitters.get(streamKey);
    // 같은 사용자가 새로 연결할 때, 이전 연결 종료
    if (existingEmitter != null) {
      // 정상적인 SSE 연결 종료
      existingEmitter.complete();
      emitters.remove(streamKey);
      stopPing(streamKey); // 기존 ping 정지
    }

    // SSE 생성
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    emitters.put(streamKey, emitter);

    // 콜백 설정 - 연결이 정상 종료될 때 실행
    emitter.onCompletion(() -> {
      log.info("SSE 연결 완료 - 사용자: {}, 세션: {}", userId, sessionId);
      emitters.remove(streamKey);
      stopPing(streamKey); // ping 정지
    });

    // 콜백 설정 - 10분 뒤 자동으로 실행되는 콜백
    emitter.onTimeout(() -> {
      log.info("SSE 연결 타임아웃 - 사용자: {}, 세션: {}", userId, sessionId);
      emitter.complete();
      stopPing(streamKey); // ping 정지
    });

    // 콜백 설정 - 예외 발생 시 실행
    emitter.onError(throwable -> {
      log.error("SSE 연결 오류 - 사용자: {}, 세션: {}", userId, sessionId, throwable);
      emitters.remove(streamKey);
      emitter.completeWithError(throwable);
      stopPing(streamKey); // ping 정지
    });

    // Ping 시작
    startPing(streamKey);

    log.info("챗봇 SSE 연결 생성 - 사용자: {}, 세션: {}", userId, sessionId);
    return emitter;
  }

  // Ping 시작
  private void startPing(String streamKey) {
    ScheduledFuture<?> pingTask = pingScheduler.scheduleAtFixedRate(() -> {
      sendPing(streamKey);
    }, PING_INTERVAL, PING_INTERVAL, TimeUnit.MILLISECONDS);
    
    pingTasks.put(streamKey, pingTask);
    log.debug("Ping 시작 - {}", streamKey);
  }

  // Ping 전송
  private void sendPing(String streamKey) {

    if (!pingTasks.containsKey(streamKey)) {
      log.debug("이미 취소된 ping 작업 무시: {}", streamKey);
      return;
    }

    SseEmitter emitter = emitters.get(streamKey);
    if (emitter != null) {
      try {
        SsePingResponse pingData = new SsePingResponse(AiMessageType.PING, null);

        emitter.send(SseEmitter.event()
          .id("ping_" + System.currentTimeMillis())
          .name("ping")
          .data(pingData));

        log.debug("Ping 전송 성공 - {}", streamKey);

      } catch (IOException e) {
        log.warn("Ping 전송 실패 - 연결 끊어짐: {}", streamKey);
        // Ping 실패 시 연결 정리
        emitters.remove(streamKey);
        stopPing(streamKey);
        emitter.completeWithError(e);
      }
    } else {
      log.debug("Ping 대상 없음 - {}", streamKey);
      stopPing(streamKey);
    }
  }

  // Ping 정지
  private void stopPing(String streamKey) {
    ScheduledFuture<?> pingTask = pingTasks.remove(streamKey);
    if (pingTask != null) {
      pingTask.cancel(false);
      log.debug("Ping 정지 - {}", streamKey);
    }
  }

  // AI 응답 청크를 SSE로 스트리밍
  public void streamAiResponse(Long userId, String sessionId, Object data) {
    String streamKey = createStreamKey(userId, sessionId);
    SseEmitter emitter = emitters.get(streamKey);

    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event()
            .id(System.currentTimeMillis() + "_" + userId)
            .name("ai_response")
            .data(data));

      } catch (IOException e) {
        log.error("SSE 이벤트 전송 실패 - 사용자: {}, 세션: {}", userId, sessionId, e);
        emitters.remove(streamKey);
        emitter.completeWithError(e);
      }
    }
  }

  private String createStreamKey(Long userId, String sessionId) {
    return userId + "_" + sessionId;
  }

  public void disconnectStream(Long userId, String sessionId) {
    String streamKey = createStreamKey(userId, sessionId);
    stopPing(streamKey); // ping 정지 추가

    SseEmitter emitter = emitters.get(streamKey);
    if (emitter != null) {
      emitter.complete();
      emitters.remove(streamKey);
    }
  }

  public int getActiveConnectionCount() {
    return emitters.size();
  }
}
