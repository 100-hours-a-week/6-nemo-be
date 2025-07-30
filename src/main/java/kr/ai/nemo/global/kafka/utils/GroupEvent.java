package kr.ai.nemo.global.kafka.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GroupEvent(
    String eventType,        // "GROUP_CREATED", "GROUP_DELETED", "GROUP_JOINED", "GROUP_LEFT"
    Object data,             // 추가 데이터 (GroupCreateResponse 등)
    LocalDateTime timestamp
) {

  public static GroupEvent groupCreated(Object data) {
    return new GroupEvent(EventType.GROUP_CREATED.name(), data, LocalDateTime.now());
  }

  public static GroupEvent groupDeleted(Object data) {
    return new GroupEvent(EventType.GROUP_DELETED.name(), data, LocalDateTime.now());
  }

  public static GroupEvent userJoined(Object data) {
    return new GroupEvent(EventType.GROUP_JOINED.name(), data, LocalDateTime.now());
  }

  public static GroupEvent userLeft(Object data) {
    return new GroupEvent(EventType.GROUP_LEFT.name(), data, LocalDateTime.now());
  }
}
