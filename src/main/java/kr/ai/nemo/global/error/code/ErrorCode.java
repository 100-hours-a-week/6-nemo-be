package kr.ai.nemo.global.error.code;

import java.io.Serializable;
import org.springframework.http.HttpStatus;

public interface ErrorCode extends Serializable {
  String getCode();
  String getMessage();
  HttpStatus getHttpStatus();
}

