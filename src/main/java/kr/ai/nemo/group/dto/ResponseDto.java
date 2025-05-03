package kr.ai.nemo.group.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDto<T> {
  private String code;
  private String message;
  private T data;

  public static <T> ResponseDto<T> success(T data) {
    return ResponseDto.<T>builder()
        .code("200")
        .message("Success")
        .data(data)
        .build();
  }

  public static <T> ResponseDto<T> fail(String code, String message) {
    return ResponseDto.<T>builder()
        .code(code)
        .message(message)
        .build();
  }
}