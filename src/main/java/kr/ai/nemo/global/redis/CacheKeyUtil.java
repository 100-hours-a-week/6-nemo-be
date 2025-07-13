package kr.ai.nemo.global.redis;

public class CacheKeyUtil {

  private static final String PREFIX = "cache";

  private CacheKeyUtil() {
    // new CacheKeyUtil 과 같은 인스턴스 생성 금지 -> 불필요한 메모리 차지
    throw new UnsupportedOperationException("Utility class");
  }

  public static String key(String namespace, Object... parts) {
    if (namespace == null || namespace.isEmpty()) {
      throw new IllegalArgumentException("namespace is null or empty");
    }
    StringBuilder builder = new StringBuilder(PREFIX).append("::").append(namespace);
    for (Object part : parts) {
      builder.append("::").append(part==null?"null":part.toString());
    }
    return builder.toString();
  }
}
