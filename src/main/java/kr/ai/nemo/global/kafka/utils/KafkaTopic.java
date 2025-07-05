package kr.ai.nemo.global.kafka.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KafkaTopic {
  GROUP_EVENT("group_event");

  private final String name;
}
