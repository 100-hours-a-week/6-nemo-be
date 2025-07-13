package kr.ai.nemo.global.redis;

import java.time.Duration;
import java.util.Random;

public class CacheJitterUtil {
  private static final Random random = new Random();

  public static Duration addJitter(Duration baseDuration, long maxJitterSeconds) {
    return baseDuration.plusSeconds(random.nextLong(maxJitterSeconds + 1));
  }

  public static Duration addJitter(Duration baseDuration, Duration maxJitter) {
    return baseDuration.plus(Duration.ofSeconds(random.nextLong(maxJitter.getSeconds() + 1)));
  }
}
