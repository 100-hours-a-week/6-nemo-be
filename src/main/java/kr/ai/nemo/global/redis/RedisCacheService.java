package kr.ai.nemo.global.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  public <T> void set(String key, T value, Duration ttl) {
    try {
      String json = objectMapper.writeValueAsString(value);
      redisTemplate.opsForValue().set(key, json, ttl);
    } catch (JsonProcessingException e) {
      log.error("redis 저장 실패: key = {}, because {}", key, e.getMessage());
    }
  }

  public <T> Optional<T> get(String key, Class<T> clazz) {
    try {
      String json = redisTemplate.opsForValue().get(key);
      if (json == null) {
        return Optional.empty();
      }
      return Optional.of(objectMapper.readValue(json, clazz));
    } catch (Exception e) {
      log.error("redis 조회 실패: key = {}, because {}", key, e.getMessage());
      return Optional.empty();
    }
  }

  public <T> void appendToList(String key, String fieldName, T newItem, Class<T> clazz) {
    try {
      String existingJson = redisTemplate.opsForValue().get(key);
      if (existingJson == null) {
        log.warn("redis append 실패: key={} 값이 없음", key);
        return;
      }

      // 전체 JSON을 ObjectNode로 파싱
      var rootNode = objectMapper.readTree(existingJson);
      var objectNode = (com.fasterxml.jackson.databind.node.ObjectNode) rootNode;

      // 기존 메시지 리스트 가져오기
      var arrayNode = (com.fasterxml.jackson.databind.node.ArrayNode) rootNode.get(fieldName);
      if (arrayNode == null) {
        arrayNode = objectMapper.createArrayNode();
      }

      // 새 메시지 추가
      var itemNode = objectMapper.valueToTree(newItem);
      arrayNode.add(itemNode);

      // 다시 덮어쓰기
      objectNode.set(fieldName, arrayNode);
      String updatedJson = objectMapper.writeValueAsString(objectNode);
      redisTemplate.opsForValue().set(key, updatedJson);

    } catch (Exception e) {
      log.error("redis 리스트 필드 추가 실패: key = {}, because {}", key, e.getMessage());
    }
  }

  public void del(String key) {
    redisTemplate.delete(key);
  }
}
