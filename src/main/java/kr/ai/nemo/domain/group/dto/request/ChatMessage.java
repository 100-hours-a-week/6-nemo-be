package kr.ai.nemo.domain.group.dto.request;

import java.util.List;
import kr.ai.nemo.domain.group.domain.enums.ChatbotRole;

public record ChatMessage (
    ChatbotRole role,
    String text,
    List<String> options
){

}
