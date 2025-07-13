package kr.ai.nemo.unit.global.kafka.messaging.kafka;

import kr.ai.nemo.unit.global.kafka.messaging.MessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaMessagePublisher implements MessagePublisher {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public CompletableFuture<Void> publishAsync(String topic, String key, Object message) {
    CompletableFuture<Void> future = new CompletableFuture<>();

    kafkaTemplate.send(topic, key, message)
        .whenComplete((result, ex) -> {
          if (ex == null) {
            log.info("✅ [Kafka] Message sent successfully to topic: {} with key: {}", topic, key);
            future.complete(null);
          } else {
            log.error("❌ [Kafka] Failed to send message to topic: {} with key: {}", topic, key, ex);
            future.completeExceptionally(new RuntimeException("Failed to publish message", ex));
          }
        });

    return future;
  }

  @Override
  public void publishSync(String topic, String key, Object message) {
    try {
      kafkaTemplate.send(topic, key, message).get();
      log.info("✅ [Kafka] Message sent synchronously to topic: {} with key: {}", topic, key);
    } catch (Exception e) {
      log.error("❌ [Kafka] Failed to send message synchronously to topic: {} with key: {}", topic,
          key, e);
      throw new RuntimeException("Failed to publish message synchronously", e);
    }
  }
}
