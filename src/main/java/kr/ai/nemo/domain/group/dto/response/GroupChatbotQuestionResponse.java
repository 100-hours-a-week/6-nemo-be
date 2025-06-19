package kr.ai.nemo.domain.group.dto.response;

import java.util.List;

public record GroupChatbotQuestionResponse(
    String questions,
    List<String> answer
) {
}
