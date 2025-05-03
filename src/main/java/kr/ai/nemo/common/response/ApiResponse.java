package kr.ai.nemo.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
  private int code;
  private String message;
  private T data;

  // 성공 응답 (200 OK, 데이터 있음)
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
  }

  // 성공 응답 (200 OK, 데이터 없음)
  public static <T> ApiResponse<T> success() {
    return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), null);
  }

  // 성공 응답 (200 OK, 커스텀 메시지)
  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), message, data);
  }

  // 특정 응답 코드 사용 (사용자 정의 코드 + 데이터)
  public static <T> ApiResponse<T> of(ResponseCode code, T data) {
    return new ApiResponse<>(code.getCode(), code.getMessage(), data);
  }

  // 특정 응답 코드 사용 (사용자 정의 코드 + 커스텀 메시지 + 데이터)
  public static <T> ApiResponse<T> of(ResponseCode code, String message, T data) {
    return new ApiResponse<>(code.getCode(), message, data);
  }

  // 오류 응답 (ErrorCode 사용)
  public static <T> ApiResponse<T> error(kr.ai.nemo.common.exception.ErrorCode errorCode) {
    return new ApiResponse<>(errorCode.getHttpStatus().value(), errorCode.getMessage(), null);
  }

  // 오류 응답 (ErrorCode + 데이터)
  public static <T> ApiResponse<T> error(kr.ai.nemo.common.exception.ErrorCode errorCode, T data) {
    return new ApiResponse<>(errorCode.getHttpStatus().value(), errorCode.getMessage(), data);
  }
}