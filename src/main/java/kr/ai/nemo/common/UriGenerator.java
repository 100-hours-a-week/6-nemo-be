package kr.ai.nemo.common;

import java.net.URI;

public class UriGenerator {

  public static URI scheduleDetail(Long scheduleId) {
    return URI.create(String.format("https://nemo.ai/api/v1/schedules/%d", scheduleId));
  }

  public static URI groupDetail(Long groupId) {
    return URI.create(String.format("https://nemo.ai/api/v1/groups/%d", groupId));
  }
}

