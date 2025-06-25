package kr.ai.nemo.domain.group.dto.response;

import java.util.List;

public record GroupChatbotSessionResponse(
    List<Message> messages
) {

  public record Message(
      String role,
      String text,
      List<String> options
  ) {
  }

}
