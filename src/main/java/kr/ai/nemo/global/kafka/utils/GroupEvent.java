package kr.ai.nemo.global.kafka.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GroupEvent(
    String eventType,        // "GROUP_CREATED", "GROUP_DELETED", "GROUP_JOINED", "GROUP_LEFT"
    Long groupId,            // nullable
    Long userId,             // nullable
    Object data,             // 추가 데이터 (GroupCreateResponse 등)
    LocalDateTime timestamp
) {

  public static GroupEvent groupCreated(Object data) {
    return new GroupEvent(EventType.GROUP_CREATED.name(), null, null, data, LocalDateTime.now());
  }

  public static GroupEvent groupDeleted(Long groupId) {
    return new GroupEvent(EventType.GROUP_DELETED.name(), groupId, null, null, LocalDateTime.now());
  }

  public static GroupEvent userJoined(Object data) {
    return new GroupEvent(EventType.GROUP_JOINED.name(), null, null, data, LocalDateTime.now());
  }

  public static GroupEvent userLeft(Object data) {
    return new GroupEvent(EventType.GROUP_LEFT.name(), null, null, data, LocalDateTime.now());
  }
}
