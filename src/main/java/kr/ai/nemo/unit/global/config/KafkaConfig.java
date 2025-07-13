package kr.ai.nemo.unit.global.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Configuration
@Slf4j
public class KafkaConfig {

  @Bean
  public ConsumerFactory<String, Object> consumerFactory(
      @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
      @Value("${spring.kafka.consumer.group-id}") String groupId) {

    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId); // application.yml에서 가져옴
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
    props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

    // JsonDeserializer 설정
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "kr.ai.nemo"); // application.yml과 동일하게
    props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

    // 컨슈머 시작 시 가장 오래 전부터 시작
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // application.yml과 동일하게

    return new DefaultKafkaConsumerFactory<>(props);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
      ConsumerFactory<String, Object> consumerFactory,
      DefaultErrorHandler errorHandler) {

    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();

    factory.setConsumerFactory(consumerFactory);
    factory.setCommonErrorHandler(errorHandler);

    return factory;
  }

  @Bean
  public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
      KafkaTemplate<String, Object> kafkaTemplate) {
    return new DeadLetterPublishingRecoverer(
        kafkaTemplate,
        (consumerRecord, ex) -> {
          String dltTopic = consumerRecord.topic() + ".DLT";
          log.warn("Sending message to DLT: {} due to: {}", dltTopic, ex.getMessage());
          return new TopicPartition(dltTopic, consumerRecord.partition());
        }
    );
  }

  @Bean
  public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
    FixedBackOff fixedBackOff = new FixedBackOff(1000L, 3); // 1초 간격, 최대 3회 재시도
    DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, fixedBackOff);

    // 재시도 로그
    handler.setRetryListeners((consumerRecord, ex, deliveryAttempt) ->
        log.warn("[Kafka Retry] Topic={}, Key={}, Attempt={}, Error={}",
            consumerRecord.topic(), consumerRecord.key(), deliveryAttempt, ex.getMessage())
    );

    // 재시도하지 않을 예외들 (즉시 DLQ로)
    handler.addNotRetryableExceptions(
        IllegalArgumentException.class,  // 잘못된 파라미터
        JsonProcessingException.class, // JSON 파싱 오류
        WebClientResponseException.class // 400 Bad Request
    );

    return handler;
  }
}
