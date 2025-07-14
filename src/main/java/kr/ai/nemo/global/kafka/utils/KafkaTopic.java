package kr.ai.nemo.global.kafka.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KafkaTopic {
  GROUP_EVENT("GROUP_EVENT"),
  GROUP_RECOMMEND_QUESTION("GROUP_RECOMMEND_QUESTION"),
  GROUP_RECOMMEND("GROUP_RECOMMEND");

  private final String name;
}
