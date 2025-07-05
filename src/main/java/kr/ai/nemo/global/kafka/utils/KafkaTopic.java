package kr.ai.nemo.global.kafka.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KafkaTopic {
  GROUP_EVENT("group_event"),
  GROUP_RECOMMEND_QUESTION("group-recommend-question"),
  GROUP_RECOMMEND("group-recommend");

  private final String name;
}
