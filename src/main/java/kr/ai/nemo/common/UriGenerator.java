package kr.ai.nemo.common;

import java.net.URI;

public class UriGenerator {

  public static URI scheduleDetail(Long scheduleId) {
    return URI.create(String.format("https://dev.nemo.ai.kr/api/v1/schedules/%d", scheduleId));
  }

  public static URI login() {
    return URI.create("https://dev.nemo.ai.kr/login");
  }
}
