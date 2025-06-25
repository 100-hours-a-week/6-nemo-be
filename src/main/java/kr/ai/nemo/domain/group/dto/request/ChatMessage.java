package kr.ai.nemo.domain.group.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import kr.ai.nemo.domain.group.domain.enums.ChatbotRole;
import lombok.Getter;

@JsonInclude(Include.NON_NULL)
@Getter
public class ChatMessage {

  ChatbotRole role;
  String text;
  List<String> options;


  public ChatMessage(ChatbotRole role, String text, List<String> options) {
    this.role = role;
    this.text = text;
    this.options = options;
  }

  public ChatMessage(ChatbotRole role, String text) {
    this.role = role;
    this.text = text;
  }
}
