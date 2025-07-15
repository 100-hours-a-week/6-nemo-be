package kr.ai.nemo.domain.group.messaging;

import kr.ai.nemo.domain.group.dto.request.GroupAiQuestionRecommendRequest;
import kr.ai.nemo.domain.group.dto.request.GroupChatbotQuestionRequest;

public interface GroupChatbotMessagePublisher {
    void publishQuestionRequest(GroupChatbotQuestionRequest request, Long userId, String sessionId);
    void publishRecommendRequest(GroupAiQuestionRecommendRequest request, String sessionId);
}
