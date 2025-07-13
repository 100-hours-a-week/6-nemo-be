package kr.ai.nemo.unit.global.kafka.producer;

import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.GroupChatbotQuestionRequest;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendQuestionRequest;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendRequest;
import kr.ai.nemo.domain.group.domain.enums.AiMessageType;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendQuestionRequest.Payload;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendRequest.RequestPayload;
import kr.ai.nemo.unit.global.kafka.utils.EventType;
import kr.ai.nemo.unit.global.kafka.utils.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatbotKafkaService {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public void sendQuestionRequest(GroupChatbotQuestionRequest request, Long userId, String sessionId) {
    GroupRecommendQuestionRequest aiRequest = new GroupRecommendQuestionRequest(
        AiMessageType.CREATE_QUESTION.getValue(),
        new Payload(sessionId, userId, request.answer())
    );

    sendToKafka(aiRequest, EventType.QUESTION_REQUEST.name(), sessionId);
  }

  public void sendRecommendRequest(GroupAiQuestionRecommendRequest request, String sessionId) {
    GroupRecommendRequest aiRequest = new GroupRecommendRequest(
        AiMessageType.RECOMMEND_REQUEST.getValue(),
        new RequestPayload(sessionId, request.userId(), request.messages())
    );

    sendToKafka(aiRequest, EventType.RECOMMEND_REQUEST.name(), sessionId);
  }

  private void sendToKafka(Object request, String requestType, String sessionId) {
    try {
      kafkaTemplate.send(KafkaTopic.GROUP_RECOMMEND_QUESTION.getName(), sessionId, request)
          .whenComplete((result, ex) -> {
            if (ex == null) {
              log.info("✅ [Kafka][{}] Request sent successfully for session: {}", requestType, sessionId);
            } else {
              log.error("❌ [Kafka][{}] Failed to send request for session: {}", requestType, sessionId, ex);
            }
          });
    } catch (Exception e) {
      log.error("❌ [Kafka][{}] Error sending request for session: {}", requestType, sessionId, e);
    }
  }
}
