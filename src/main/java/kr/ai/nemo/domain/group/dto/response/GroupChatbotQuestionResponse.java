package kr.ai.nemo.domain.group.dto.response;

import java.util.List;

public record GroupChatbotQuestionResponse(
    String question,
    List<String> options
) {
}
