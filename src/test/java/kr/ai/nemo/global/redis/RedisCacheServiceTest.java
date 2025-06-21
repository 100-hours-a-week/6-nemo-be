package kr.ai.nemo.global.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisCacheService 테스트")
class RedisCacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisCacheService redisCacheService;

    @Test
    @DisplayName("[성공] 객체 저장")
    void set_Success() throws JsonProcessingException {
        // given
        String key = "test:key";
        TestObject value = new TestObject("test", 123);
        Duration ttl = Duration.ofMinutes(10);
        String json = "{\"name\":\"test\",\"value\":123}";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(objectMapper.writeValueAsString(value)).willReturn(json);

        // when
        redisCacheService.set(key, value, ttl);

        // then
        verify(objectMapper).writeValueAsString(value);
        verify(valueOperations).set(key, json, ttl);
    }

    @Test
    @DisplayName("[성공] 객체 조회")
    void get_Success() throws Exception {
        // given
        String key = "test:key";
        String json = "{\"name\":\"test\",\"value\":123}";
        TestObject expectedObject = new TestObject("test", 123);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn(json);
        given(objectMapper.readValue(json, TestObject.class)).willReturn(expectedObject);

        // when
        Optional<TestObject> result = redisCacheService.get(key, TestObject.class);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("test");
        assertThat(result.get().getValue()).isEqualTo(123);
        verify(valueOperations).get(key);
        verify(objectMapper).readValue(json, TestObject.class);
    }

    @Test
    @DisplayName("[성공] 객체 조회 - 값이 없음")
    void get_NoValue_ReturnEmpty() {
        // given
        String key = "test:key";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn(null);

        // when
        Optional<TestObject> result = redisCacheService.get(key, TestObject.class);

        // then
        assertThat(result).isEmpty();
        verify(valueOperations).get(key);
    }

    @Test
    @DisplayName("[실패] 객체 조회 - JSON 파싱 실패")
    void get_JsonParsingException_ReturnEmpty() throws Exception {
        // given
        String key = "test:key";
        String invalidJson = "invalid json";

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn(invalidJson);
        given(objectMapper.readValue(invalidJson, TestObject.class))
                .willThrow(new RuntimeException("JSON parsing failed"));

        // when
        Optional<TestObject> result = redisCacheService.get(key, TestObject.class);

        // then
        assertThat(result).isEmpty();
        verify(valueOperations).get(key);
        verify(objectMapper).readValue(invalidJson, TestObject.class);
    }

    @Test
    @DisplayName("[성공] 키 삭제")
    void del_Success() {
        // given
        String key = "test:key";

        // when
        redisCacheService.del(key);

        // then
        verify(redisTemplate).delete(key);
    }

    @Test
    @DisplayName("[실패] 리스트에 항목 추가 - 키가 존재하지 않음")
    void appendToList_KeyNotExists_LogWarning() {
        // given
        String key = "test:key";
        String fieldName = "messages";
        TestObject newItem = new TestObject("new", 456);
        Duration ttl = Duration.ofMinutes(10);

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn(null);

        // when
        redisCacheService.appendToList(key, fieldName, newItem, TestObject.class, ttl);

        // then
        verify(valueOperations).get(key);
        // 경고 로그가 기록되고 조기 반환
    }

    @Test
    @DisplayName("[실패] 객체 저장 - JSON 변환 실패")
    void set_JsonProcessingException_LogError() throws JsonProcessingException {
        // given
        String key = "test:key";
        TestObject value = new TestObject("test", 123);
        Duration ttl = Duration.ofMinutes(10);

        given(objectMapper.writeValueAsString(value))
                .willThrow(new JsonProcessingException("JSON processing failed") {});

        // when
        redisCacheService.set(key, value, ttl);

        // then
        verify(objectMapper).writeValueAsString(value);
    }

    // 테스트용 객체
    private static class TestObject {
        private String name;
        private int value;

        public TestObject() {}

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
