package kr.ai.nemo.global.testUtil;

import java.lang.reflect.Field;

public class TestReflectionUtils {
  /**
   * 테스트용으로 엔티티의 private long 타입 필드에 값을 주입하는 헬퍼 메서드
   * @param targetEntity - 값을 주입할 객체
   * @param fieldName - 필드 이름 (예: "id")
   * @param value - 주입할 값 (예: 1L)
   */
  public static void setField(Object targetEntity, String fieldName, Object value) {
    try {
      Field field = targetEntity.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(targetEntity, value);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("Failed to set id via reflection", e);
    }
  }
}
