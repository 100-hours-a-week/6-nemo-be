package kr.ai.nemo.domain.group.messaging.impl;

import kr.ai.nemo.domain.group.domain.enums.AiMessageType;
import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.GroupChatbotQuestionRequest;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendQuestionRequest;
import kr.ai.nemo.domain.group.dto.websocket.request.GroupRecommendRequest;
import kr.ai.nemo.domain.group.messaging.GroupChatbotMessagePublisher;
import kr.ai.nemo.unit.global.kafka.utils.EventType;
import kr.ai.nemo.unit.global.kafka.utils.KafkaTopic;
import kr.ai.nemo.unit.global.kafka.messaging.MessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChatbotMessagePublisherImpl implements GroupChatbotMessagePublisher {
    
    private final MessagePublisher messagePublisher;
    
    @Override
    public void publishQuestionRequest(GroupChatbotQuestionRequest request, Long userId, String sessionId) {
        GroupRecommendQuestionRequest aiRequest = new GroupRecommendQuestionRequest(
            AiMessageType.CREATE_QUESTION.getValue(),
            new GroupRecommendQuestionRequest.Payload(sessionId, userId, request.answer())
        );
        
        messagePublisher.publishAsync(
            KafkaTopic.GROUP_RECOMMEND_QUESTION.getName(), 
            sessionId, 
            aiRequest
        ).thenRun(() -> 
            log.info("✅ [{}] Question request published for session: {}", 
                EventType.QUESTION_REQUEST.name(), sessionId)
        ).exceptionally(ex -> {
            log.error("❌ [{}] Failed to publish question request for session: {}", 
                EventType.QUESTION_REQUEST.name(), sessionId, ex);
            return null;
        });
    }
    
    @Override
    public void publishRecommendRequest(GroupAiQuestionRecommendRequest request, String sessionId) {
        GroupRecommendRequest aiRequest = new GroupRecommendRequest(
            AiMessageType.RECOMMEND_REQUEST.getValue(),
            new GroupRecommendRequest.RequestPayload(sessionId, request.userId(), request.messages())
        );
        
        messagePublisher.publishAsync(
            KafkaTopic.GROUP_RECOMMEND_QUESTION.getName(), 
            sessionId, 
            aiRequest
        ).thenRun(() -> 
            log.info("✅ [{}] Recommend request published for session: {}", 
                EventType.RECOMMEND_REQUEST.name(), sessionId)
        ).exceptionally(ex -> {
            log.error("❌ [{}] Failed to publish recommend request for session: {}", 
                EventType.RECOMMEND_REQUEST.name(), sessionId, ex);
            return null;
        });
    }
}
