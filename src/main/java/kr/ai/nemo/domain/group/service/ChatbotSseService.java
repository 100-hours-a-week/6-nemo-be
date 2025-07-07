package kr.ai.nemo.domain.group.service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotSseService {

  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 10; // SSE 연결 유지 시간 10분

  public SseEmitter createStream(Long userId, String sessionId) {
    String streamKey = createStreamKey(userId, sessionId);

    SseEmitter existingEmitter = emitters.get(streamKey);
    // 같은 사용자가 새로 연결할 때, 이전 연결 종료
    if (existingEmitter != null) {
      // 정상적인 SSE 연결 종료
      existingEmitter.complete();
      emitters.remove(streamKey);
    }

    // SSE 생성
    SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
    emitters.put(streamKey, emitter);

    // 콜백 설정 - 연결이 정상 종료될 때 실행
    emitter.onCompletion(() -> {
      log.info("SSE 연결 완료 - 사용자: {}, 세션: {}", userId, sessionId);
      emitters.remove(streamKey);
    });

    // 콜백 설정 - 60분 뒤 자동으로 실행되는 콜백
    emitter.onTimeout(() -> {
      log.info("SSE 연결 타임아웃 - 사용자: {}, 세션: {}", userId, sessionId);
      emitter.complete();
    });

    // 콜백 설정 - 예외 발생 시 실행
    emitter.onError(throwable -> {
      log.error("SSE 연결 오류 - 사용자: {}, 세션: {}", userId, sessionId, throwable);
      emitters.remove(streamKey);
      emitter.completeWithError(throwable);
    });

    log.info("챗봇 SSE 연결 생성 - 사용자: {}, 세션: {}", userId, sessionId);
    return emitter;
  }

  // AI 응답 청크를 SSE로 스트리밍
  public void streamAiResponse(Long userId, String sessionId, String chunk, boolean isComplete) {
    String streamKey = createStreamKey(userId, sessionId);
    SseEmitter emitter = emitters.get(streamKey);

    if (emitter != null) {
      try {
        Map<String, Object> eventData = Map.of(
            "type", isComplete ? "RESPONSE_COMPLETE" : "RESPONSE_CHUNK",
            "chunk", chunk,
            "isComplete", isComplete,
            "timestamp", System.currentTimeMillis()
        );

        emitter.send(SseEmitter.event()
            .id(System.currentTimeMillis() + "_" + userId)
            .name("ai_response")
            .data(eventData));

        if (isComplete) {
          // 응답 완료 시 연결 종료하지 않고 유지 (다음 질문 대기)
          log.info("AI 응답 완료 - 사용자: {}, 세션: {}", userId, sessionId);
        }

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
