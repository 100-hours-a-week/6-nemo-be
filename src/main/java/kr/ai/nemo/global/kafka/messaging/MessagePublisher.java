package kr.ai.nemo.global.kafka.messaging;

import java.util.concurrent.CompletableFuture;

public interface MessagePublisher {

  /**
   * 메시지를 비동기로 발행합니다.
   *
   * @param topic   토픽 이름
   * @param key     메시지 키
   * @param message 메시지 내용
   * @return 발행 결과를 담은 CompletableFuture
   */
  CompletableFuture<Void> publishAsync(String topic, String key, Object message);

  /**
   * 메시지를 동기로 발행합니다.
   *
   * @param topic   토픽 이름
   * @param key     메시지 키
   * @param message 메시지 내용
   */
  void publishSync(String topic, String key, Object message);
}
