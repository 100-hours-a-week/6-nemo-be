package kr.ai.nemo.unit.global.redis;

import java.time.Duration;

public class CacheConstants {
  public static final String REDIS_CHATBOT_PREFIX = "chatbot";
  public static final String REDIS_CHATBOT_MESSAGES_FIELD = "messages";
  public static final Duration CHATBOT_SESSION_TTL = Duration.ofMinutes(30);
}
