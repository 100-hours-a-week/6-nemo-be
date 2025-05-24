package kr.ai.nemo.global.common;

import kr.ai.nemo.global.error.code.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
  private int code;
  private String message;
  private T data;

  // 성공 응답 생성자 (errorCode 필드 추가로 인한 생성자 추가)
  public ApiResponse(int code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  // 성공 응답 (200 OK, 데이터 있음)
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(SuccessCode.SUCCESS.getHttpStatus().value(),SuccessCode.SUCCESS.getMessage(), data);
  }

  // 성공 응답 (201 Created, 데이터 있음)
  public static <T> ApiResponse<T> created(T data) {
    return new ApiResponse<>(SuccessCode.CREATED.getHttpStatus().value(),SuccessCode.CREATED.getMessage(), data);
  }

  // 성공 응답 (204 No Content, 데이터 없음)
  public static <T> ApiResponse<T> noContent() {
    return new ApiResponse<>(SuccessCode.NO_CONTENT.getHttpStatus().value(), SuccessCode.NO_CONTENT.getMessage(), null);
  }

  // 오류 응답 (ErrorCode 사용)
  public static <T> ApiResponse<T> error(ErrorCode errorCode) {
    return new ApiResponse<>(errorCode.getHttpStatus().value(), errorCode.getMessage(), null);
  }

  public static <T> ApiResponse<T> error(int code, String message) {
    return new ApiResponse<>(code, message, null);
  }
}
