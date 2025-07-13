package kr.ai.nemo.global.common;

import kr.ai.nemo.global.error.code.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseApiResponse<T> {
  private int code;
  private String message;
  private T data;

  // 성공 응답 (200 OK, 데이터 있음)
  public static <T> BaseApiResponse<T> success(T data) {
    return new BaseApiResponse<>(SuccessCode.SUCCESS.getHttpStatus().value(),SuccessCode.SUCCESS.getMessage(), data);
  }

  // 성공 응답 (201 Created, 데이터 있음)
  public static <T> BaseApiResponse<T> created(T data) {
    return new BaseApiResponse<>(SuccessCode.CREATED.getHttpStatus().value(),SuccessCode.CREATED.getMessage(), data);
  }

  // 성공 응답 (204 No Content, 데이터 없음)
  public static <T> BaseApiResponse<T> noContent() {
    return new BaseApiResponse<>(SuccessCode.NO_CONTENT.getHttpStatus().value(), SuccessCode.NO_CONTENT.getMessage(), null);
  }

  // 오류 응답 (ErrorCode 사용)
  public static <T> BaseApiResponse<T> error(ErrorCode errorCode) {
    return new BaseApiResponse<>(errorCode.getHttpStatus().value(), errorCode.getMessage(), null);
  }

  public static <T> BaseApiResponse<T> error(int code, String message) {
    return new BaseApiResponse<>(code, message, null);
  }
}
