package kr.ai.nemo.common.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    return new ApiResponse<>(ResponseCode.SUCCESS.getHttpStatus().value(),ResponseCode.SUCCESS.getMessage(), data);
  }

  // 성공 응답 (201 Created, 데이터 있음)
  public static <T> ApiResponse<T> created(T data) {
    return new ApiResponse<>(ResponseCode.CREATED.getHttpStatus().value(),ResponseCode.CREATED.getMessage(), data);
  }

  // 성공 응답 (204 No Content, 데이터 없음)
  public static <T> ApiResponse<T> noContent() {
    return new ApiResponse<>(ResponseCode.NO_CONTENT.getHttpStatus().value(), ResponseCode.NO_CONTENT.getMessage(), null);
  }

  // 오류 응답 (ErrorCode 사용)
  public static <T> ApiResponse<T> error(ResponseCode responseCode) {
    return new ApiResponse<>(responseCode.getHttpStatus().value(), responseCode.getMessage(), null);
  }
}
