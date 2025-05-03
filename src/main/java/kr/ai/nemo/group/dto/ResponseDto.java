package kr.ai.nemo.group.dto;

import static kr.ai.nemo.common.enums.SuccessResponseCode.SUCCESS;

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
        .code(SUCCESS.getCode())
        .message(SUCCESS.getMessage())
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