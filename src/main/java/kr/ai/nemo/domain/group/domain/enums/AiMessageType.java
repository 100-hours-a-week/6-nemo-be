package kr.ai.nemo.domain.group.domain.enums;

import lombok.Getter;

@Getter
public enum AiMessageType {
  CREATE_QUESTION("CREATE_QUESTION"),
  QUESTION_CHUNK("QUESTION_CHUNK"),
  QUESTION_OPTIONS("QUESTION_OPTIONS"),
  RECOMMEND_REQUEST("RECOMMEND_REQUEST"),
  RECOMMEND_ID("RECOMMEND_ID"),
  RECOMMEND_REASON("RECOMMEND_REASON"),
  RECOMMEND_DONE("RECOMMEND_DONE"),
  ERROR("ERROR");

  private final String value;

  AiMessageType(String value) {
    this.value = value;
  }
}
