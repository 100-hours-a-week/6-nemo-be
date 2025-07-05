package kr.ai.nemo.global.kafka.utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EventType {
  GROUP_CREATED("GROUP_CREATED"),
  GROUP_DELETED("GROUP_DELETED"),
  GROUP_JOINED("GROUP_JOINED"),
  GROUP_LEFT("GROUP_LEFT");

  private final String value;
}
