package kr.ai.nemo.global.redis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CacheConstants 테스트")
class CacheConstantsTest {

    @Test
    @DisplayName("[성공] Redis 챗봇 프리픽스 확인")
    void redisChatbotPrefix_IsCorrect() {
        // when & then
        assertThat(CacheConstants.REDIS_CHATBOT_PREFIX).isEqualTo("chatbot");
    }

    @Test
    @DisplayName("[성공] Redis 챗봇 메시지 필드 확인")
    void redisChatbotMessagesField_IsCorrect() {
        // when & then
        assertThat(CacheConstants.REDIS_CHATBOT_MESSAGES_FIELD).isEqualTo("messages");
    }

    @Test
    @DisplayName("[성공] 챗봇 세션 TTL 확인")
    void chatbotSessionTtl_IsCorrect() {
        // when & then
        assertThat(CacheConstants.CHATBOT_SESSION_TTL).isEqualTo(Duration.ofMinutes(30));
        assertThat(CacheConstants.CHATBOT_SESSION_TTL.toMinutes()).isEqualTo(30);
        assertThat(CacheConstants.CHATBOT_SESSION_TTL.getSeconds()).isEqualTo(1800);
    }

    @Test
    @DisplayName("[성공] 상수들이 null이 아님")
    void constants_AreNotNull() {
        // when & then
        assertThat(CacheConstants.REDIS_CHATBOT_PREFIX).isNotNull();
        assertThat(CacheConstants.REDIS_CHATBOT_MESSAGES_FIELD).isNotNull();
        assertThat(CacheConstants.CHATBOT_SESSION_TTL).isNotNull();
    }

    @Test
    @DisplayName("[성공] 상수들이 빈 문자열이 아님")
    void stringConstants_AreNotEmpty() {
        // when & then
        assertThat(CacheConstants.REDIS_CHATBOT_PREFIX).isNotEmpty();
        assertThat(CacheConstants.REDIS_CHATBOT_MESSAGES_FIELD).isNotEmpty();
    }

    @Test
    @DisplayName("[성공] TTL이 양수임")
    void ttl_IsPositive() {
        // when & then
        assertThat(CacheConstants.CHATBOT_SESSION_TTL.isNegative()).isFalse();
        assertThat(CacheConstants.CHATBOT_SESSION_TTL.isZero()).isFalse();
        assertThat(CacheConstants.CHATBOT_SESSION_TTL.toMinutes()).isGreaterThan(0);
    }
}
