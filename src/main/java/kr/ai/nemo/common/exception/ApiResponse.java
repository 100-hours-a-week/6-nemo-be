package kr.ai.nemo.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
  private int code;
  private String message;
  private T data;
  private String errorCode;

  // 성공 응답 생성자 (errorCode 필드 추가로 인한 생성자 추가)
  public ApiResponse(int code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
    this.errorCode = null; // 성공 응답에서는 errorCode가 null
  }

  // 성공 응답 (200 OK, 데이터 있음)
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(ResponseCode.SUCCESS.getHttpStatus().value(),ResponseCode.SUCCESS.getMessage(), data);
  }

  // 성공 응답 (201 Created, 데이터 있음)
  public static <T> ApiResponse<T> created(T data) {
    return new ApiResponse<>(ResponseCode.SUCCESS.getHttpStatus().value(),ResponseCode.SUCCESS.getMessage(), data);
  }

  // 성공 응답 (204 No Content, 데이터 없음)
  public static <T> ApiResponse<T> noContent() {
    return new ApiResponse<>(ResponseCode.SUCCESS.getHttpStatus().value(), ResponseCode.NO_CONTENT.getMessage(), null);
  }

  // 오류 응답 (ErrorCode 사용)
  public static <T> ApiResponse<T> error(ResponseCode responseCode) {
    return new ApiResponse<>(responseCode.getHttpStatus().value(), responseCode.getMessage(), null);
  }
  
  // 오류 응답 (메시지와 에러코드 직접 지정)
  public static <T> ApiResponse<T> error(String message, String errorCode) {
    return ApiResponse.<T>builder()
            .code(HttpStatus.BAD_REQUEST.value())
            .message(message)
            .data(null)
            .errorCode(errorCode)
            .build();
  }
  
  // 오류 응답 (메시지만 직접 지정)
  public static <T> ApiResponse<T> error(String message) {
    return ApiResponse.<T>builder()
            .code(HttpStatus.BAD_REQUEST.value())
            .message(message)
            .data(null)
            .errorCode("ERROR")
            .build();
  }
}