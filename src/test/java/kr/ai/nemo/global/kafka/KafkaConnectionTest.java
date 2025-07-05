package kr.ai.nemo.global.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Properties;

@ActiveProfiles("local")
@SpringBootTest
public class KafkaConnectionTest {

  @Test
  public void testKafkaConnection() {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.ACKS_CONFIG, "all");  // idempotent producer를 위해 all로 설정
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

    try (Producer<String, String> producer = new KafkaProducer<>(props)) {
      ProducerRecord<String, String> record = new ProducerRecord<>("group_event", "test-key", "test-message");
      producer.send(record).get();
      System.out.println("✅ Kafka connection successful!");
    } catch (Exception e) {
      System.err.println("❌ Kafka connection failed: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
