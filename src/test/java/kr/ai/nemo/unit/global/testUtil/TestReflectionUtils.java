package kr.ai.nemo.unit.global.testUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

  /**
   * 테스트용으로 객체의 private 메서드를 호출하는 헬퍼 메서드
   * @param targetObject - 메서드를 호출할 객체
   * @param methodName - 메서드 이름 (예: "onCreate")
   * @param parameters - 메서드 파라미터 (없으면 빈 배열)
   */
  public static void callMethod(Object targetObject, String methodName, Object... parameters) {
    try {
      Class<?>[] parameterTypes = new Class<?>[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        parameterTypes[i] = parameters[i].getClass();
      }
      
      Method method = targetObject.getClass().getDeclaredMethod(methodName, parameterTypes);
      method.setAccessible(true);
      method.invoke(targetObject, parameters);
    } catch (Exception e) {
      throw new RuntimeException("Failed to call method via reflection: " + methodName, e);
    }
  }
}
